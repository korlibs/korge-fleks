plugins {
    kotlin("multiplatform")
//    alias(libs.plugins.kotlin.multiplatform) apply false  -- TODO: check if this works with Gradle 9
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
    applyDefaultHierarchyTemplate()
    jvm {}

    sourceSets {
        commonMain.dependencies {
            api(libs.korge)

            implementation(libs.kaml)
            implementation(libs.fleks)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
