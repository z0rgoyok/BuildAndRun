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
            implementation(libs.decompose)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
