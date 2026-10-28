plugins {
    id("logronobus.kmp.library")
}

kotlin {
    sourceSets {
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
        }

        commonMain.dependencies {
            api(libs.jetbrains.annotations)
            api(libs.napier)
            implementation(libs.kotlinx.datetime)
        }
        wasmJsMain.dependencies {
            implementation(npm("@js-joda/timezone", "2.25.2"))
        }
    }
}
