plugins {
    id("logronobus.kmp.library")
    id("logronobus.koin")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.grant.core)
            implementation(libs.play.services.location)
        }
        commonMain.dependencies {
            implementation(project(":core:common"))
            api(project(":core:model"))
            implementation(libs.kotlinx.coroutinesCore)
            implementation(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.annotations)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }
        iosMain.dependencies {
            implementation(libs.grant.core)
        }
    }
}
