plugins {
    `kotlin-dsl`
}

group = "dev.adrc95.logronobus.buildlogic"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.composeCompiler.gradlePlugin)
    compileOnly(libs.serialization.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.koin.gradlePlugin)
    compileOnly(libs.spotless.gradlePlugin)
}

gradlePlugin {
    plugins {
        create("kmpLibrary") {
            id = "logronobus.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        create("kmpCompose") {
            id = "logronobus.kmp.compose"
            implementationClass = "KmpComposeConventionPlugin"
        }
        create("koin") {
            id = "logronobus.koin"
            implementationClass = "KoinConventionPlugin"
        }
        create("serialization") {
            id = "logronobus.serialization"
            implementationClass = "SerializationConventionPlugin"
        }
        create("kmpNavigation") {
            id = "logronobus.kmp.navigation"
            implementationClass = "KmpNavigationConventionPlugin"
        }
        create("kmpFeature") {
            id = "logronobus.kmp.feature"
            implementationClass = "KmpFeatureConventionPlugin"
        }
        create("androidApplication") {
            id = "logronobus.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        create("spotless") {
            id = "logronobus.spotless"
            implementationClass = "SpotlessConventionPlugin"
        }
        create("moduleGraph") {
            id = "logronobus.module-graph"
            implementationClass = "dev.adrc95.logronobus.buildlogic.ModuleGraphPlugin"
        }
    }
}
