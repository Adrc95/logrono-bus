plugins {
    id("logronobus.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.immutable.collections)
        }
    }
}
