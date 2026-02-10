import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(projects.presentation.root)
            export(projects.presentation.common)
            export(projects.presentation.navigation)
            export(projects.presentation.context.repositories)
            export(projects.presentation.context.worktrees)
            export(projects.presentation.context.settings)
            export(projects.presentation.context.editors)
            export(projects.presentation.context.gitactions)
            export(projects.presentation.context.groups)
            export(projects.presentation.context.kanban)
            export(projects.presentation.context.sidebar)
            export(projects.presentation.context.messages)
            export(projects.presentation.context.texts)
            export(projects.domain.shared)
            export(projects.domain.context.kanban)
            export(projects.domain.context.worktrees)
        }
    }

    listOf(
        macosArm64(),
        macosX64(),
    ).forEach { macosTarget ->
        macosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = false
            export(projects.presentation.root)
            export(projects.presentation.common)
            export(projects.presentation.navigation)
            export(projects.presentation.context.repositories)
            export(projects.presentation.context.worktrees)
            export(projects.presentation.context.settings)
            export(projects.presentation.context.editors)
            export(projects.presentation.context.gitactions)
            export(projects.presentation.context.groups)
            export(projects.presentation.context.kanban)
            export(projects.presentation.context.sidebar)
            export(projects.presentation.context.messages)
            export(projects.presentation.context.texts)
            export(projects.domain.shared)
            export(projects.domain.context.kanban)
            export(projects.domain.context.worktrees)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.application.shared)
            implementation(projects.application.context.repositories)
            implementation(projects.application.context.worktrees)
            implementation(projects.domain.shared)
            implementation(projects.domain.context.copy)
            implementation(projects.domain.context.editors)
            implementation(projects.domain.context.kanban)
            implementation(projects.domain.context.repositories)
            implementation(projects.domain.context.worktrees)
            implementation(projects.presentation.resources)
            implementation(libs.decompose)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            api(projects.presentation.root)
            api(projects.presentation.common)
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
            api(projects.domain.shared)
            api(projects.domain.context.kanban)
            api(projects.domain.context.worktrees)
        }
        commonTest.dependencies {
            implementation(projects.presentation.common)
            implementation(projects.presentation.resources)
            implementation(projects.presentation.root)
            implementation(libs.kotlin.test)
        }
    }
}
