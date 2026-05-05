import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun providerProperty(name: String, defaultValue: String = ""): String {
    return providers.gradleProperty(name)
        .orElse(providers.provider { localProperties.getProperty(name) ?: defaultValue })
        .get()
}

android {
    namespace = "com.x.myapplication"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.x.myapplication"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        fun quoted(value: String) = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

        val externalServerBaseUrl = providerProperty("EXTERNAL_SERVER_BASE_URL", "https://example.com")
        val pleosClientId = providerProperty("PLEOS_CLIENT_ID")
        val pleosClientSecret = providerProperty("PLEOS_CLIENT_SECRET")

        buildConfigField("String", "EXTERNAL_SERVER_BASE_URL", quoted(externalServerBaseUrl))
        buildConfigField("String", "PLEOS_CLIENT_ID", quoted(pleosClientId))
        buildConfigField("String", "PLEOS_CLIENT_SECRET", quoted(pleosClientSecret))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.material)
    implementation(libs.pleos.navi.helper)
    implementation(libs.pleos.speech.to.text)
    implementation(libs.pleos.text.to.speech)
    implementation(libs.pleos.vehicle)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
