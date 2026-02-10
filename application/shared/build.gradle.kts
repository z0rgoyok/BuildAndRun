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
            implementation(projects.domain.context.copy)
            implementation(projects.domain.context.editors)
            implementation(projects.domain.context.kanban)
            implementation(projects.domain.context.repositories)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
