package dev.adrc95.logronobus.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class GraphDumpTask : DefaultTask() {

    @get:Input
    abstract val projectPath: Property<String>

    @get:Input
    abstract val dependencies: MapProperty<String, Set<Pair<String, String>>>

    @get:Input
    abstract val plugins: MapProperty<String, PluginType>

    @get:OutputFile
    abstract val output: RegularFileProperty

    @get:OutputFile
    abstract val legend: RegularFileProperty

    override fun getDescription(): String = "Dumps project dependencies to a mermaid file."

    @TaskAction
    operator fun invoke() {
        output.get().asFile.writeText(mermaid())
        legend.get().asFile.writeText(legend())
    }

    private fun mermaid(): String = buildString {
        appendLine(
            """
            ---
            config:
              layout: elk
              elk:
                nodePlacementStrategy: SIMPLE
            ---
            """.trimIndent(),
        )
        appendLine("graph TB")

        val closure = reachable()
        val nodes = dependencies.get().values
            .flatMap { entries -> entries.map { it.second } }
            .toSet()
            .plus(dependencies.get().keys)
            .plus(projectPath.get())
            .filter { it in closure }
            .toSet()

        val members = mutableMapOf<String, MutableSet<String>>()
        val children = mutableMapOf<String, MutableSet<String>>()
        val loose = mutableSetOf<String>()

        nodes.sorted().forEach { node ->
            val group = node.substringBeforeLast(":")
            if (group.isEmpty()) {
                loose += node
            } else {
                members.getOrPut(group) { mutableSetOf() } += node
                children.getOrPut(group.substringBeforeLast(":")) { mutableSetOf() } += group
            }
        }

        fun render(group: String, indent: Int) {
            appendLine(" ".repeat(indent) + "subgraph $group")
            appendLine(" ".repeat(indent) + "  direction TB")
            members[group].orEmpty().sorted().forEach { node -> appendLine(node.alias(indent + 2)) }
            children[group].orEmpty().sortedWith(ownGroupFirst()).forEach { child -> render(child, indent + 2) }
            appendLine(" ".repeat(indent) + "end")
        }

        children[""].orEmpty().sortedWith(ownGroupFirst()).forEach { group -> render(group, indent = 2) }
        loose.sorted().forEach { node -> appendLine(node.alias(indent = 2)) }

        val links = links()
        if (links.isNotEmpty()) appendLine()
        links.forEach { appendLine(it.render(indent = 2)) }

        appendLine()
        PluginType.entries.forEach { appendLine(it.classDef()) }
    }

    private fun legend(): String = buildString {
        appendLine("graph TB")
        listOf(
            "feature" to PluginType.Feature,
            "navigation" to PluginType.Navigation,
            "library" to PluginType.Library,
            "application" to PluginType.AndroidApplication,
            "jvm" to PluginType.KotlinJvm,
        ).forEach { (name, type) -> appendLine(name.alias(indent = 2, type = type)) }
        appendLine()
        listOf(
            Link("feature", "-->", "navigation"),
            Link("feature", "-->", "library"),
            Link("application", "-->", "library"),
            Link("library", "-.->", "jvm"),
        ).forEach { appendLine(it.render(indent = 2)) }
        appendLine()
        PluginType.entries.forEach { appendLine(it.classDef()) }
    }

    private fun reachable(): Set<String> {
        val deps = dependencies.get()
        val seen = mutableSetOf<String>()
        val queue = ArrayDeque(listOf(projectPath.get()))
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (!seen.add(current)) continue
            deps[current].orEmpty().forEach { (_, target) -> queue.addLast(target) }
        }
        return seen
    }

    private fun ownGroupFirst(): Comparator<String> {
        val own = projectPath.get().substringBeforeLast(":")
        return compareBy({ if (it == own) 0 else 1 }, { it })
    }

    private fun links(): List<Link> {
        val closure = reachable()
        return dependencies.get()
            .flatMap { (from, entries) -> entries.map { (configuration, to) -> Triple(from, to, configuration) } }
            .groupBy { (from, to, _) -> from to to }
            .map { (pair, edges) ->
                val configurations = edges.map { (_, _, configuration) -> configuration }
                Link(pair.first, configurations.arrow(), pair.second)
            }
            .filter { it.project in closure && it.dependency in closure }
            .sortedWith(compareBy({ it.project }, { it.dependency }))
    }

    private fun List<String>.arrow(): String = when {
        any { it.endsWith(API) } -> "-->"
        any { it.endsWith(IMPLEMENTATION) } -> "-.->"
        else -> "-.->|${first()}|"
    }

    private fun String.alias(indent: Int, type: PluginType = plugins.get().getValue(this)): String =
        " ".repeat(indent) + this + "[" + substringAfterLast(":") + "]:::" + type.ref

    private fun PluginType.classDef(): String = "classDef $ref $style;"

    private data class Link(val project: String, val arrow: String, val dependency: String) {
        fun render(indent: Int): String = " ".repeat(indent) + "$project $arrow $dependency"
    }

    private companion object {
        const val API = "Api"
        const val IMPLEMENTATION = "Implementation"
    }
}

@CacheableTask
abstract class GraphUpdateTask : DefaultTask() {

    @get:Input
    abstract val projectPath: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val input: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val legend: RegularFileProperty

    @get:OutputFile
    abstract val output: RegularFileProperty

    override fun getDescription(): String = "Updates the README of the module with its dependency graph."

    @TaskAction
    operator fun invoke() {
        val file = output.get().asFile
        if (!file.exists()) {
            file.writeText(
                """
                # `${projectPath.get()}`

                ## Module dependency graph

                $START $END

                """.trimIndent(),
            )
        }
        val mermaid = input.get().asFile.readText().trimTrailingNewLines()
        val legend = legend.get().asFile.readText().trimTrailingNewLines()
        val block = buildString {
            appendLine(START)
            appendLine("```mermaid")
            appendLine(mermaid)
            appendLine("```")
            appendLine()
            appendLine("<details><summary>📋 Graph legend</summary>")
            appendLine()
            appendLine("```mermaid")
            appendLine(legend)
            appendLine("```")
            appendLine()
            appendLine("</details>")
            appendLine(END)
        }.trimEnd()
        file.writeText(file.readText().replace(REGION, block))
    }

    private fun String.trimTrailingNewLines(): String =
        lines().dropLastWhile(String::isBlank).joinToString("\n")

    private companion object {
        const val START = "<!--region graph-->"
        const val END = "<!--endregion-->"
        val REGION = Regex("$START.*?$END", RegexOption.DOT_MATCHES_ALL)
    }
}
