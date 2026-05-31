plugins {
        alias(libs.plugins.korge)
        alias(libs.plugins.kotlin.serialization)
    }

/*
korge {
    // Mark this module as library so that Korge does not search for a main.kt entry point
    id = "korge.fleks.library"
}
*/

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
        jvm()
    
        sourceSets {
                commonMain.dependencies {
                        implementation(libs.kaml)
                        implementation(libs.fleks)
                        implementation(libs.kotlinx.serialization.json)
                    }
            }
    }

/*
var javaVersion = 25

kotlin {
    jvmToolchain(javaVersion)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}
*/