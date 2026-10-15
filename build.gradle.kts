// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // On remplace 2.1.20 par 1.9.24 pour être stable avec Room
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("androidx.navigation.safeargs") version "2.7.7" apply false
    id("com.google.dagger.hilt.android") version "2.52" apply false
}