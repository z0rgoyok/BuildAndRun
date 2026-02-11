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
            implementation(projects.application.shared)
            implementation(projects.application.context.repositories)
            implementation(projects.domain.shared)
            implementation(projects.domain.context.editors)
            implementation(projects.presentation.common)
            implementation(projects.presentation.resources)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
