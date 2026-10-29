rootProject.name = "LogronoBus"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven {
            name = "localMaplibreWasm"
            url = uri("local-maven")
            content { includeGroupAndSubgroups("org.maplibre.compose") }
        }
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":app:androidApp")
project(":app:androidApp").projectDir = file("app/androidApp")
include(":core:data")
include(":core:navigation")
include(":core:device")
include(":core:database")
include(":core:designsystem")
include(":core:preference")
include(":core:common")
include(":core:domain")
include(":core:testing")
include(":core:ui")
include(":core:model")
include(":core:network")
include(":app:desktopApp")
project(":app:desktopApp").projectDir = file("app/desktopApp")
include(":app:shared")
project(":app:shared").projectDir = file("app/shared")
include(":app:webApp")
project(":app:webApp").projectDir = file("app/webApp")
include(":feature:fares")
include(":feature:fares:navigation")
include(":feature:settings")
include(":feature:settings:navigation")
include(":feature:stops")
include(":feature:stops:navigation")
include(":feature:favourites")
include(":feature:favourites:navigation")
include(":feature:buses")
include(":feature:buses:navigation")
include(":feature:nearby")
include(":feature:nearby:navigation")
include(":feature:routes")
include(":feature:routes:navigation")
include(":feature:tracking")
include(":feature:tracking:navigation")
include(":feature:scan")
include(":feature:scan:navigation")
