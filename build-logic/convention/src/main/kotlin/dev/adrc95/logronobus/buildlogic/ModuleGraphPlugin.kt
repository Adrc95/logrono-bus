package dev.adrc95.logronobus.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class ModuleGraphPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        if (target.path != ":") return
        target.subprojects.forEach { subproject -> subproject.configureGraphTasks() }
    }
}

internal fun Project.configureGraphTasks() {
    if (!buildFile.exists()) return
    val dump = tasks.register<GraphDumpTask>("graphDump") {
        val graph = Graph(this@configureGraphTasks).invoke()
        projectPath.set(this@configureGraphTasks.path)
        dependencies.set(graph.dependencies())
        plugins.set(graph.plugins())
        output.set(this@configureGraphTasks.layout.buildDirectory.file("mermaid/graph.txt"))
        legend.set(this@configureGraphTasks.layout.buildDirectory.file("mermaid/legend.txt"))
    }
    tasks.register<GraphUpdateTask>("graphUpdate") {
        projectPath.set(this@configureGraphTasks.path)
        input.set(dump.flatMap { it.output })
        legend.set(dump.flatMap { it.legend })
        output.set(this@configureGraphTasks.layout.projectDirectory.file("README.md"))
    }
}
