import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.Sync
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.koinCompiler) apply false
    alias(libs.plugins.spotless) apply false
    id("logronobus.module-graph")
}

val webWorker = rootDir.resolve("core/database/src/wasmJsMain/web-worker")

val syncSqliteWebWorker = tasks.register<Sync>("syncSqliteWebWorker") {
    description = "Mirrors the vendored SQLite web worker over the copy in node_modules"
    from(webWorker)
    // The root npm world of the wasm target; the web is the only target that ships the worker.
    into(layout.buildDirectory.dir("wasm/node_modules/@logronobus/sqlite-web-worker"))
    mustRunAfter(tasks.matching { it.name == "kotlinWasmNpmInstall" })
}

// Ktor's JS/wasm client pins ws 8.20.1 (still on Ktor main), which is in the range of a ws security
// advisory; Dependabot can't bump it because kotlin-js-store/wasm has a lockfile but no package.json.
// The browser app never loads ws (it uses the browser's own WebSocket), but override it to a fixed
// release so the lock stops carrying the vulnerable version. Drop this once Ktor bumps ws itself.
plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnPlugin> {
    the<org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnRootExtension>().resolution(
        "ws",
        "8.22.0"
    )
}

subprojects {
    configurations.matching { it.name.contains("wasm", ignoreCase = true) }.configureEach {
        resolutionStrategy.dependencySubstitution {
            substitute(module("org.maplibre.compose:maplibre-compose"))
                .using(module("org.maplibre.compose:maplibre-compose:${libs.versions.maplibre.wasm.get()}"))
        }
    }

    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin-test")) {
                useVersion(libs.versions.kotlin.get())
            }
        }
    }

    tasks.withType<KotlinWebpack>().configureEach {
        dependsOn(syncSqliteWebWorker)
        inputs.dir(webWorker).withPathSensitivity(PathSensitivity.RELATIVE)
    }
    // Runs webpack for the browser test bundle; it is Kotlin's own task type, hence by name.
    tasks.matching { it.name == "prepareWebpackBundleForKotlinJsTests" }.configureEach {
        dependsOn(syncSqliteWebWorker)
        inputs.dir(webWorker).withPathSensitivity(PathSensitivity.RELATIVE)
    }
    // With the Karma runner the browser test task bundles the tests itself, so it is the one that
    // embeds worker.js in the bundle under test (RoomOnWebTest opens the database through it).
    tasks.matching { it.name == "wasmJsBrowserTest" }.configureEach {
        dependsOn(syncSqliteWebWorker)
        inputs.dir(webWorker).withPathSensitivity(PathSensitivity.RELATIVE)
    }
}