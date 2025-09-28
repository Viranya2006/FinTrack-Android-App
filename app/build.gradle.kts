plugins {
    alias(libs.plugins.android.application)
    // --- Add this line ---
    alias(libs.plugins.google.services)}

android {
    namespace = "com.example.fintrack"
    // CHANGE THIS from 35 or 36
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fintrack"
        minSdk = 24
        // CHANGE THIS from 35 or 36
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // --- Add these lines ---
    // Firebase Bill of Materials (BOM)
    implementation(platform(libs.firebase.bom))
    // Firebase Authentication
    implementation(libs.firebase.auth)
    // Cloud Firestore Database
    implementation(libs.firebase.firestore)

    // Charting Library
    implementation(libs.mpandroidchart)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.androidx.biometric)
    implementation("de.hdodenhof:circleimageview:3.1.0")


}