plugins {
    id("logronobus.kmp.library")
    id("logronobus.koin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            api(project(":core:model"))
            api(libs.datastore.preferences.core)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.annotations)
            implementation(libs.koin.core)
        }
        wasmJsMain.dependencies {
            implementation(libs.datastore.core.okio)
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }
    }
}
