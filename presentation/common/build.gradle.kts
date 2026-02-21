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
            implementation(projects.application.context.worktrees)
            api(projects.domain.shared)
            implementation(projects.domain.context.copy)
            implementation(projects.domain.context.editors)
            api(projects.domain.context.kanban)
            implementation(projects.domain.context.repositories)
            api(projects.domain.context.worktrees)
            implementation(projects.presentation.resources)
            implementation(libs.compose.components.resources)
            implementation(libs.decompose)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
