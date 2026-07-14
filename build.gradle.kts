import korlibs.korge.gradle.*


plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlin.serialization)
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
