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
            implementation(projects.domain.shared)
            implementation(projects.domain.context.worktrees)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
