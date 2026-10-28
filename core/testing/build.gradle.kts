plugins {
    id("logronobus.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:domain"))
            api(project(":core:network"))
            api(project(":core:data"))
            api(project(":core:database"))
            api(project(":core:preference"))
            api(libs.datastore.preferences.core)
            api(libs.kotlinx.coroutinesTest)
            api(libs.kotlinx.coroutinesCore)
            api(libs.ktor.client.core)
            api(libs.ktor.client.mock)
            api(libs.ktorfit.lib)
            api(libs.ktor.client.contentNegotiation)
            api(libs.ktor.serialization.kotlinxJson)
            api(libs.kotlinx.serializationJson)
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.kotlin.test)
        }
    }
}
