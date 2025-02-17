buildscript {
    repositories {
        google()
        mavenCentral()

        maven {
            url = uri("https://jitpack.io") // Correct way to specify the URL
        }
    }
    dependencies {
        val nav_version = "2.5.2"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")



    }
}

plugins {
    id("com.android.application") version "8.1.2" apply false
    id("com.android.library") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}
