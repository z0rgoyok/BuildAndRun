package app.tich.buildandrun.appstore

enum class AppChild {
    WORKSPACE,
    SETTINGS,
    HELP,
}

enum class AppSheetKind {
    ADD_REPOSITORY,
    ADD_WORKTREE,
    CREATE_PR,
    COMPLETE_WORKTREE,
    CONFIGURE_EDITORS,
    HELP,
}

data class AppSheetState(
    val kind: AppSheetKind,
    val worktreePath: String? = null,
)
