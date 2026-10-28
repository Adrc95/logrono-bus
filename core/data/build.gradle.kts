plugins {
    id("logronobus.kmp.library")
    id("logronobus.koin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:domain"))
            api(project(":core:common"))
            implementation(project(":core:network"))
            implementation(project(":core:device"))
            implementation(project(":core:database"))
            implementation(project(":core:preference"))
            implementation(libs.kotlinx.coroutinesCore)
            implementation(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.annotations)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
            implementation(project(":core:testing"))
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
            implementation(project(":core:testing"))
        }
    }
}

tasks.named<Test>("jvmTest") {
    systemProperty(
        "logronobus.data.dir",
        layout.buildDirectory.dir("jvmTest-data").get().asFile.absolutePath
    )
}
