plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

buildscript {
    repositories {
        mavenLocal()
        maven { url = uri("https://central.sonatype.com/repository/maven-snapshots") }
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

kotlin {
    jvm {}

    sourceSets {
        commonMain.dependencies {
            implementation(libs.korge)
            implementation(libs.kaml)
            implementation(libs.fleks)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
