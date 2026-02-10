plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    listOf(
        macosArm64(),
        macosX64(),
    ).forEach { macosTarget ->
        macosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = false
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.application)
            implementation(projects.domain)
            implementation(libs.decompose)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
        }
        macosMain.dependencies {
            implementation(projects.presentation.root)
            implementation(projects.presentation.runtime)
        }
        commonTest.dependencies {
            implementation(projects.presentation.common)
            implementation(projects.presentation.resources)
            implementation(projects.presentation.runtime)
            implementation(projects.presentation.root)
            implementation(libs.kotlin.test)
        }
    }
}
