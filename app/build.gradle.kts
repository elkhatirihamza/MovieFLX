plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

android {
    namespace = "com.iacontrol.movieflx"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.iacontrol.movieflx"
        minSdk = 28
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Au lieu de libs...., mettez la version "en dur" :
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // --- CORRECTION ROOM & SQLITE ---
    // On utilise les versions standards définies dans le TOML corrigé
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    // Le compilateur doit utiliser la même version (gérée par le TOML)
    kapt(libs.androidx.room.compiler)

    // NOTE : On a SUPPRIMÉ sqlite-jvm, room-common-jvm et room-runtime-android
    // Room inclut automatiquement la bonne version de SQLite pour Android.

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.glide)

    // Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-compiler:2.52")

    implementation("io.coil-kt:coil:2.5.0")
}