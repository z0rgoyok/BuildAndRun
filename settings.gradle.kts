rootProject.name = "buildandrun"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

include(":bridge")
include(":domain:shared")
include(":domain:context:copy")
include(":domain:context:editors")
include(":domain:context:kanban")
include(":domain:context:repositories")
include(":domain:context:worktrees")
include(":application:shared")
include(":application:context:repositories")
include(":application:context:worktrees")
include(":presentation:resources")
include(":presentation:common")
include(":presentation:navigation")
include(":presentation:runtime")
include(":presentation:context:repositories")
include(":presentation:context:worktrees")
include(":presentation:context:settings")
include(":presentation:context:editors")
include(":presentation:context:gitactions")
include(":presentation:context:groups")
include(":presentation:context:kanban")
include(":presentation:context:sidebar")
include(":presentation:context:messages")
include(":presentation:context:texts")
include(":presentation:root")
