package app.tich.buildandrun.presentation.app

data class AppSheetState(
    val kind: AppSheetKind,
    val worktreePath: String? = null,
)
