plugins {
    id("com.android.application")
}

android {
    compileSdk = 34
    namespace = "com.example.desktopapp"

    defaultConfig {
        applicationId = "com.example.desktopapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
//    implementation("ai.openai:gpt-turbo:3.5.0")
    implementation("com.android.volley:volley:1.2.1")
//    implementation("ai.openai:gpt:1.1.3") // Add this line
    implementation("com.squareup.okhttp3:okhttp:4.9.3") // Add this line
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

}

