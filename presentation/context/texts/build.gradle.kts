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
            implementation(projects.presentation.common)
            implementation(projects.presentation.resources)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
