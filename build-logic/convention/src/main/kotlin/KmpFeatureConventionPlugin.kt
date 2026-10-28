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
 
import dev.adrc95.logronobus.buildlogic.libs
import dev.adrc95.logronobus.buildlogic.requiredLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("logronobus.kmp.compose")
            pluginManager.apply("logronobus.koin")
            pluginManager.apply("logronobus.serialization")

            dependencies {
                add("commonMainImplementation", libs.requiredLibrary("compose-uiToolingPreview"))
                add("commonMainImplementation", libs.requiredLibrary("compose-runtime"))
                add("commonMainImplementation", libs.requiredLibrary("compose-foundation"))
                add("commonMainImplementation", libs.requiredLibrary("compose-material3"))
                add("commonMainImplementation", libs.requiredLibrary("compose-ui"))
                add("commonMainImplementation", libs.requiredLibrary("compose-components-resources"))
                add(
                    "commonMainImplementation",
                    libs.requiredLibrary("androidx-lifecycle-viewmodelCompose")
                )
                add(
                    "commonMainImplementation",
                    libs.requiredLibrary("androidx-lifecycle-runtimeCompose")
                )
                add("commonMainImplementation", libs.requiredLibrary("kotlinx-coroutinesCore"))
                add("commonMainImplementation", libs.requiredLibrary("kotlinx-datetime"))
                add("commonMainImplementation", libs.requiredLibrary("kotlinx-serializationJson"))
                add("commonMainImplementation", libs.requiredLibrary("navigation3-runtime"))
                add(
                    "commonMainImplementation",
                    platform(libs.requiredLibrary("koin-bom"))
                )
                add("commonMainApi", libs.requiredLibrary("koin-annotations"))
                add("commonMainImplementation", libs.requiredLibrary("koin-core"))
                add("commonMainImplementation", libs.requiredLibrary("koin-compose"))
                add("commonMainImplementation", libs.requiredLibrary("koin-compose-viewmodel"))

                add("commonTestImplementation", libs.requiredLibrary("kotlin-test"))
                add("commonTestImplementation", libs.requiredLibrary("kotlinx-coroutinesTest"))
                add("commonTestImplementation", libs.requiredLibrary("turbine"))
                add("commonTestImplementation", project(":core:testing"))
            }
        }
    }
}
