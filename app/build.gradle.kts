import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

val hyConnectBaseUrl: String =
    localProperties.getProperty("HYCONNECT_BASE_URL") ?: "https://example.invalid/"

android {
    namespace = "com.hyconnect.pleos"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hyconnect.pleos"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("boolean", "USE_DUMMY_DATA", "false")
        // 실제 배포 서버 주소는 Git에 커밋하지 않는 local.properties의 HYCONNECT_BASE_URL에서 읽는다.
        // Retrofit baseUrl은 반드시 /로 끝나야 한다.
        buildConfigField("String", "HYCONNECT_BASE_URL", "\"$hyConnectBaseUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // 서버가 활성화되어(dashboard/sufficient 포함) 디버그에서도 실제 서버와 통신한다.
            // 서버 없이 더미 데이터로만 화면을 검증하려면 true로 바꾼다.
            buildConfigField("boolean", "USE_DUMMY_DATA", "false")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("boolean", "USE_DUMMY_DATA", "false")
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
    // 운전습관 분석용 차량 속도(PERF_VEHICLE_SPEED_DISPLAY)는 Google android.car로 받는다.
    useLibrary("android.car")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.coroutines.android)
    // 운전습관 프로파일 로컬 영속 저장.
    implementation(libs.androidx.datastore.preferences)
    implementation("ai.pleos.playground:NaviHelper:2.0.3")
    implementation("ai.pleos.playground:Vehicle:2.0.3")
    // Gleo AI 음성 안내(TTS)·음성 입력(STT). 정책상 OnDevice 모드만 사용한다.
    implementation("ai.pleos.playground:TextToSpeech:2.1.5.1")
    implementation("ai.pleos.playground:SpeechToText:2.1.3.2")
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
