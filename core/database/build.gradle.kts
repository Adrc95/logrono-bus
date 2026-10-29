@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("logronobus.kmp.library")
    id("logronobus.koin")
    alias(libs.plugins.room3)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    android {
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    wasmJs {
        browser {
            testTask {
                useKarma {
                    useConfigDirectory(file("karma.config.d"))
                    useChromeHeadless()
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(libs.room3.runtime)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.annotations)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.sqlite.bundled)
        }
        iosMain.dependencies {
            implementation(libs.sqlite.bundled)
        }
        jvmMain.dependencies {
            implementation(libs.sqlite.bundled)
        }
        wasmJsMain.dependencies {
            implementation(libs.sqlite.web)
            implementation(libs.kotlinx.browser)
            implementation(
                npm(
                    "@logronobus/sqlite-web-worker",
                    "$projectDir/src/wasmJsMain/web-worker"
                )
            )
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
            implementation(project(":core:testing"))
        }
        iosTest.dependencies {
            implementation(libs.sqlite.bundled)
        }

        jvmTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.sqlite.bundled)
        }
        wasmJsTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.sqlite.web)
        }

        getByName("androidDeviceTest") {
            dependsOn(getByName("nonAndroidTest"))
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.androidx.testRunner)
                implementation(libs.sqlite.bundled)
            }
        }
    }
}

tasks.named("wasmJsBrowserTest") {
    inputs.dir("karma.config.d")
}

dependencies {
    add("kspAndroid", libs.room3.compiler)
    add("kspJvm", libs.room3.compiler)
    add("kspIosArm64", libs.room3.compiler)
    add("kspIosSimulatorArm64", libs.room3.compiler)
    add("kspWasmJs", libs.room3.compiler)
}

tasks.withType<org.gradle.api.tasks.testing.AbstractTestTask>().configureEach {
    failOnNoDiscoveredTests = false
}

tasks.named<Test>("jvmTest") {
    onlyIf {
        val os = System.getProperty("os.name").orEmpty()
        val arch = System.getProperty("os.arch").orEmpty()
        val macosX86 = os.startsWith("Mac") && (arch == "x86_64" || arch == "i386")
        !macosX86
    }
    doFirst {
        logger.lifecycle("sqlite-bundled has no native for macos x86: the DAO tests are skipped on this host")
    }
}
