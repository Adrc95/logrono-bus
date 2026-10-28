package dev.adrc95.logronobus.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency

internal class Graph(
    private val root: Project,
    private val dependencies: MutableMap<Project, Set<Pair<Configuration, Project>>> = mutableMapOf(),
    private val plugins: MutableMap<Project, PluginType> = mutableMapOf(),
    private val seen: MutableSet<String> = mutableSetOf(),
) {

    private val ignoredProjects = root.providers.gradleProperty("graph.ignoredProjects")
        .map { it.split(",").map(String::trim).filter(String::isNotEmpty).toSet() }
        .orElse(emptySet())

    private val supportedConfigurations =
        root.providers.gradleProperty("graph.supportedConfigurations")
            .map { it.split(",").map(String::trim).filter(String::isNotEmpty).toSet() }
            .orElse(emptySet())

    operator fun invoke(project: Project = root): Graph {
        if (project.path in seen) return this
        seen += project.path
        plugins.putIfAbsent(
            project,
            PluginType.entries.firstOrNull { project.pluginManager.hasPlugin(it.id) } ?: PluginType.Unknown,
        )
        dependencies.compute(project) { _, previous -> previous.orEmpty() }
        project.configurations
            .matching { it.isSupported() }
            .forEach { configuration ->
                configuration.dependencies
                    .withType(ProjectDependency::class.java)
                    .map { dependency -> configuration to project.project(dependency.path) }
                    .filter { (_, target) -> target.path !in ignoredProjects.get() }
                    .forEach { (source, target) ->
                        dependencies.compute(project) { _, previous -> previous.orEmpty() + (source to target) }
                        invoke(target)
                    }
            }
        return this
    }

    fun dependencies(): Map<String, Set<Pair<String, String>>> = dependencies
        .mapKeys { it.key.path }
        .mapValues { (_, values) ->
            values.mapTo(mutableSetOf()) { (configuration, target) -> configuration.name to target.path }
        }

    fun plugins(): Map<String, PluginType> = plugins.mapKeys { it.key.path }

    private fun Configuration.isSupported(): Boolean {
        val declared = supportedConfigurations.get()
        if (declared.isNotEmpty()) return name in declared
        if (!name.endsWith(API) && !name.endsWith(IMPLEMENTATION)) return false
        // Test source sets use the same suffixes (`commonTestImplementation`, `jvmTestImplementation`),
        // so without this every fixtures dependency (`:core:testing`) would pull the whole graph in.
        return NON_PRODUCTION_MARKERS.none { name.contains(it, ignoreCase = true) }
    }

    private companion object {
        const val API = "Api"
        const val IMPLEMENTATION = "Implementation"
        val NON_PRODUCTION_MARKERS = listOf("test", "benchmark")
    }
}

enum class PluginType(val id: String, val ref: String, val style: String) {
    Feature(
        id = "logronobus.kmp.feature",
        ref = "feature",
        style = "fill:#CAFFBF,stroke:#000,stroke-width:2px,color:#000",
    ),
    Navigation(
        id = "logronobus.kmp.navigation",
        ref = "navigation",
        style = "fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000",
    ),
    Library(
        id = "logronobus.kmp.library",
        ref = "library",
        style = "fill:#BDB2FF,stroke:#000,stroke-width:2px,color:#000",
    ),
    AndroidApplication(
        id = "com.android.application",
        ref = "application",
        style = "fill:#FFD6A5,stroke:#000,stroke-width:2px,color:#000",
    ),
    KotlinJvm(
        id = "org.jetbrains.kotlin.jvm",
        ref = "jvm-library",
        style = "fill:#FDFFB6,stroke:#000,stroke-width:2px,color:#000",
    ),
    KotlinMultiplatform(
        id = "org.jetbrains.kotlin.multiplatform",
        ref = "kmp-library",
        style = "fill:#FFC6FF,stroke:#000,stroke-width:2px,color:#000",
    ),
    AndroidLibrary(
        id = "com.android.kotlin.multiplatform.library",
        ref = "android-library",
        style = "fill:#E0E0E0,stroke:#000,stroke-width:2px,color:#000",
    ),
    Unknown(
        id = "?",
        ref = "unknown",
        style = "fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000",
    ),
}
