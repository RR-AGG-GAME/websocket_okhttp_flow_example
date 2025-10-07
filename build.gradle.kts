plugins {
    kotlin("jvm") version "1.8.22" apply false
    id("com.android.application") version "8.1.1" apply false
    id("com.android.library") version "8.1.1" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
