import java.io.FileInputStream
import java.util.Properties

plugins {
    id("logronobus.android.application")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
try {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
} catch (e: Exception) {
}

val versionMajor = 1
val versionMinor = 1

val versionNum: String? = findProperty("versionNum") as String?

fun versionCode(): Int {
    versionNum?.let {
        val code: Int = (versionMajor * 1000000) + (versionMinor * 1000) + it.toInt()
        println("versionCode is set to $code")
        return code
    } ?: return 1
}

fun versionName(): String {
    versionNum?.let {
        val name = "$versionMajor.$versionMinor.$versionNum"
        println("versionName is set to $name")
        return name
    } ?: return "1.0"
}

dependencies {
    implementation(projects.shared)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "dev.adrc95.logronobus"

    signingConfigs {
        val signingStore: File? = (keystoreProperties["storeFile"] as String?)
            ?.let { file(it) }
            ?.takeIf { it.exists() }

        getByName("debug") {
            if (signingStore != null) {
                storeFile = signingStore
                keyAlias = keystoreProperties["keyAlias"] as String?
                keyPassword = keystoreProperties["keyPassword"] as String?
                storePassword = keystoreProperties["storePassword"] as String?
            }
        }
        create("release") {
            if (signingStore != null) {
                storeFile = signingStore
                keyAlias = keystoreProperties["keyAlias"] as String?
                keyPassword = keystoreProperties["keyPassword"] as String?
                storePassword = keystoreProperties["storePassword"] as String?
            }
            enableV2Signing = true
        }
    }

    defaultConfig {
        applicationId = "dev.adrc95.logronobus"
        versionCode = versionCode()
        versionName = versionName()
    }
    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
