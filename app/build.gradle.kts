plugins {
    alias(libs.plugins.android.application)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.turgo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.turgo"
        minSdk = 30
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))

    // When using the BoM, you don't specify versions in Firebase library dependencies

    // Add the dependency for the Firebase SDK for Google Analytics
    implementation(libs.firebase.analytics)

    // TODO: Add the dependencies for any other Firebase products you want to use
    // See https://firebase.google.com/docs/android/setup#available-libraries
    // For example, add the dependencies for Firebase Authentication and Cloud Firestore
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.fragment)
    implementation(libs.material.v1100)
    //implementation(libs.firebase.admin)
    implementation (libs.google.auth.library.credentials)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.firebase.database)
    implementation(libs.google.services)

    //implementation(libs.google.firebase.auth)
    //implementation(libs.google.firebase.firestore)
    implementation(libs.play.services.auth)


    // Jetpack Compose integration
    implementation(libs.navigation.compose)

    // Views/Fragments integration
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Feature module support for Fragments
    implementation(libs.navigation.dynamic.features.fragment)

    // Testing Navigation
    androidTestImplementation(libs.navigation.testing)

    // JSON serialization library, works with the Kotlin serialization plugin
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.zxing.android.embedded)
}