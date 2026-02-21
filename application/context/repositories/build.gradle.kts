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
            implementation(projects.application.context.worktrees)
            implementation(projects.domain.shared)
            implementation(projects.domain.context.copy)
            implementation(projects.domain.context.kanban)
            implementation(projects.domain.context.repositories)
            implementation(projects.domain.context.worktrees)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
