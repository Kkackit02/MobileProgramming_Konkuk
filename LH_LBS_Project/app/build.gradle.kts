import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.secrets.gradle.plugin)
    alias(libs.plugins.google.gms.google.services)
}
val secretProps = Properties().apply {
    val file = rootProject.file("secrets.properties")
    if (file.exists()) {
        file.inputStream().use { this.load(it) } // ✅ 명확하게 this.load 사용!
    }
}


android {
    namespace = "com.example.lh_lbs_project"
    compileSdk = 35

    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.lh_lbs_project"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "NAVER_CLIENT_ID", "\"${secretProps["NAVER_CLIENT_ID"] ?: ""}\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"${secretProps["NAVER_CLIENT_SECRET"] ?: ""}\"")
        resValue("string", "NAVER_CLIENT_ID", "\"${secretProps["NAVER_CLIENT_ID"] ?: ""}\"")
        resValue("string", "NAVER_CLIENT_SECRET", "\"${secretProps["NAVER_CLIENT_SECRET"] ?: ""}\"")

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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

secrets {
    propertiesFileName ="secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}
dependencies {

    implementation("com.naver.maps:map-sdk:3.21.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.functions)
    implementation(libs.map.sdk)
    implementation(libs.naver.map.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20210307")
}