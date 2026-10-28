/*
 * Copyright 2026 adrc95 (Adrián Cabrera)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.android.build.api.dsl.ApplicationExtension
import dev.adrc95.logronobus.buildlogic.libs
import dev.adrc95.logronobus.buildlogic.requiredInt
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            pluginManager.apply("logronobus.spotless")

            extensions.configure<ApplicationExtension> {
                compileSdk = libs.requiredInt("android-compileSdk")
                compileSdkMinor = libs.requiredInt("android-compileSdkMinor")

                defaultConfig {
                    minSdk = libs.requiredInt("android-minSdk")
                    targetSdk = libs.requiredInt("android-targetSdk")
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                buildFeatures {
                    compose = true
                }

                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }
            }

            // AGP 9 brings its own Kotlin support, so there is no kotlin-android plugin to apply:
            // the JVM target lives on the Kotlin extension that AGP registers.
            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }
}
