package dev.adrc95.logronobus.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@OptIn(ExperimentalWasmDsl::class)
internal fun Project.configureKotlinMultiplatform() {
    val compileSdkVersion = libs.requiredInt("android-compileSdk")
    val minSdkVersion = libs.requiredInt("android-minSdk")
    val namespace = "dev.adrc95.logronobus" + path.replace(':', '.')

    extensions.configure<KotlinMultiplatformExtension> {
        jvm()
        iosArm64()
        iosSimulatorArm64()
        wasmJs {
            browser()
        }
        applyDefaultHierarchyTemplate()

        val android = (this as ExtensionAware).extensions.getByName("android")
            as KotlinMultiplatformAndroidLibraryTarget
        android.namespace = namespace
        android.compileSdk = compileSdkVersion
        android.minSdk = minSdkVersion
        android.compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }
}
