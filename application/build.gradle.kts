plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Application"
            isStatic = true
        }
    }

    listOf(
        macosArm64(),
        macosX64(),
    ).forEach { macosTarget ->
        macosTarget.binaries.framework {
            baseName = "Application"
            isStatic = false
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.domain)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
