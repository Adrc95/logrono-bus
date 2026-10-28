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
 
import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class  SpotlessConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.diffplug.spotless")

            extensions.configure<SpotlessExtension> {
                kotlin {
                    target("src/**/*.kt")
                    ktlint()
                    licenseHeaderFile(rootProject.file("spotless/license.kt"))
                    trimTrailingWhitespace()
                    endWithNewline()
                }
                format("kts") {
                    target("*.kts")
                    target("src/**/*.kts")
                    licenseHeaderFile(
                        rootProject.file("spotless/license.kt"),
                        // The first line that is neither blank nor part of a block comment.
                        "(^(?!\\s*$)(?![\\/ ]\\*).*$)"
                    )
                    trimTrailingWhitespace()
                    endWithNewline()
                }
                format("xml") {
                    target("src/**/*.xml")
                    licenseHeaderFile(rootProject.file("spotless/license.xml"), "(<[^!?])")
                    trimTrailingWhitespace()
                    endWithNewline()
                }
            }
        }
    }
}
