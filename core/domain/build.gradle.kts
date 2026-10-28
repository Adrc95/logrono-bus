plugins {
    id("logronobus.kmp.library")
    id("logronobus.koin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            api(project(":core:model"))
            implementation(libs.kotlinx.coroutinesCore)
            api(libs.kotlinx.datetime)
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.annotations)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
            implementation(project(":core:testing"))
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
            implementation(project(":core:testing"))
        }
    }
}
