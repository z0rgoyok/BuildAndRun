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
            implementation(projects.presentation.navigation)
            implementation(projects.presentation.runtime)
            implementation(projects.presentation.context.repositories)
            implementation(projects.presentation.context.worktrees)
            implementation(projects.presentation.context.settings)
            implementation(projects.presentation.context.editors)
            implementation(projects.presentation.context.gitactions)
            implementation(projects.presentation.context.groups)
            implementation(projects.presentation.context.kanban)
            implementation(projects.presentation.context.sidebar)
            implementation(projects.presentation.context.messages)
            implementation(projects.presentation.context.texts)
            implementation(libs.decompose)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
