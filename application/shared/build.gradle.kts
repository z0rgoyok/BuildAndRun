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
            implementation(projects.domain.shared)
            implementation(projects.domain.context.editors)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
