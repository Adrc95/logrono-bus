plugins {
    id("logronobus.kmp.compose")
}

kotlin {
    android {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        val maplibreMain = create("maplibreMain") {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.maplibre.compose)
                implementation(libs.kotlinx.serializationJson)
            }
        }
        androidMain.get().dependsOn(maplibreMain)
        iosMain.get().dependsOn(maplibreMain)

        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:designsystem"))
            implementation(project(":core:common"))
            implementation(project(":core:domain"))
            implementation(libs.compose.runtime)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            api(libs.compose.components.resources)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.kotlinx.datetime)
        }

        androidMain.dependencies {
            implementation(libs.maplibre.runtime.android)
        }

        wasmJsMain.dependencies {
            implementation(libs.maplibre.compose)
            implementation(libs.kotlinx.serializationJson)
            implementation(libs.kotlinx.browser)
            implementation(npm("maplibre-gl", "6.6.0"))
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(project(":core:testing"))
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.compose.components.resources)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "dev.adrc95.logronobus.core.ui.generated.resources"
}
