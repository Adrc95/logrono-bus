import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("logronobus.spotless")
}

dependencies {
    implementation(projects.app.shared)

    implementation(libs.compose.desktop)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "dev.adrc95.logronobus.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dev.adrc95.logronobus"
            packageVersion = "1.0.0"

            macOS {
                iconFile.set(project.file("icons/app.icns"))
            }
            windows {
                iconFile.set(project.file("icons/app.ico"))
            }
            linux {
                iconFile.set(project.file("icons/app-icon.png"))
            }
        }
    }
}