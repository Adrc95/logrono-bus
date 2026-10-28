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
 
import dev.adrc95.logronobus.buildlogic.configureKotlinMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply("com.android.kotlin.multiplatform.library")
            pluginManager.apply("logronobus.spotless")
            configureKotlinMultiplatform()

            // AGP's lint tasks read KSP's generated sources but do not declare the dependency on
            // them; Gradle 9 fails the build instead of warning, so the edge is added here for
            // whichever KSP tasks the module happens to register.
            tasks
                .matching { task ->
                    (task.name.startsWith("generate") && task.name.endsWith("LintModel")) ||
                        task.name.startsWith("lintAnalyze") ||
                        task.name.startsWith("lintVitalAnalyze")
                }.configureEach {
                    dependsOn(tasks.matching { task -> task.name.startsWith("ksp") })
                }
        }
    }
}
