@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("logronobus.kmp.compose")
    id("logronobus.koin")
    id("logronobus.serialization")
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            binaryOption("bundleId", "dev.adrc95.logronobus.shared")
        }
    }

    wasmJs {
        // Compose's checkComposeUiTestConfigurationForWasmJs requires an executable binary so
        // webpack bundles the Skiko runtime into the test bundle (CMP-4906).
        binaries.executable()
    }

    android {
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:data"))
            implementation(project(":core:device"))
            implementation(project(":feature:fares"))
            implementation(project(":feature:settings"))
            implementation(project(":feature:stops"))
            implementation(project(":feature:favourites"))
            implementation(project(":feature:buses"))
            implementation(project(":feature:nearby"))
            implementation(project(":feature:routes"))
            implementation(project(":feature:tracking"))
            implementation(project(":feature:scan"))
            implementation(project(":core:database"))
            implementation(project(":core:preference"))
            implementation(project(":core:common"))
            implementation(project(":core:domain"))
            implementation(project(":core:ui"))
            implementation(project(":core:designsystem"))
        }

        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material3.adaptive.navigation.suite)
            implementation(libs.compose.navigationEvent)
            implementation(project(":core:navigation"))
            implementation(libs.jetlime)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serializationJson)
            implementation(project(":core:network"))
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.annotations)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }
        jvmTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.compose.uiTest)
            implementation(libs.compose.desktop)
        }
    }
}

tasks.named<Test>("jvmTest") {
    systemProperty(
        "logronobus.data.dir",
        layout.buildDirectory.dir("jvmTest-data").get().asFile.absolutePath
    )
}

koinCompiler {
    // The plugin writes its cross-module hints for the Native and Wasm compilations but they never
    // make it into the klib, so every `koinViewModel` here sees the feature view models as missing
    // on Apple and Wasm. Upstream and still open: koin-compiler-plugin#105, #106, #113. There is no
    // per-compilation switch; drop this when the plugin ships the fix.
    compileSafety = false
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
