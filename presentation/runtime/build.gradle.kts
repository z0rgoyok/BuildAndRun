plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()
    macosArm64()
    macosX64()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.application)
            implementation(projects.domain)
            implementation(projects.presentation.common)
            implementation(projects.presentation.resources)
            implementation(libs.decompose)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
