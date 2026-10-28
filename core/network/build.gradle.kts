plugins {
    id("logronobus.kmp.library")
    id("logronobus.koin")
    id("logronobus.serialization")
    alias(libs.plugins.ktorfit)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            api(project(":core:common"))
            api(project(":core:model"))
            implementation(project(":core:domain"))
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinxJson)
            implementation(libs.ktorfit.lib)
            implementation(libs.ktorfit.converters.response)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.kotlinx.io.core)
            implementation(libs.kotlinx.serializationJson)
            implementation(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.annotations)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
            implementation(project(":core:testing"))
            implementation("io.ktor:ktor-client-mock:${libs.versions.ktor.get()}")
            implementation(project(":core:domain"))
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.kotlinx.datetime)
            implementation(project(":core:testing"))
        }
    }
}
