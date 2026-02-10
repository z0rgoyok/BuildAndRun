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
            api(projects.presentation.common)
            implementation(projects.presentation.resources)
            api(projects.presentation.navigation)
            api(projects.presentation.context.repositories)
            api(projects.presentation.context.worktrees)
            api(projects.presentation.context.settings)
            api(projects.presentation.context.editors)
            api(projects.presentation.context.gitactions)
            api(projects.presentation.context.groups)
            api(projects.presentation.context.kanban)
            api(projects.presentation.context.sidebar)
            api(projects.presentation.context.messages)
            api(projects.presentation.context.texts)
            implementation(libs.decompose)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
