package app.tich.buildandrun.appstore

internal fun AppStoreCore.onSelectChild(child: AppChild) {
    if (activeChild == child) {
        return
    }
    activeChild = child
    publishState()
}

internal fun AppStoreCore.onPresentSheet(
    kind: AppSheetKind,
    worktreePath: String?,
) {
    val normalizedWorktreePath = worktreePath?.trim()?.ifBlank { null }
    if (kind == AppSheetKind.CREATE_PR || kind == AppSheetKind.COMPLETE_WORKTREE) {
        require(normalizedWorktreePath != null) {
            "worktreePath is required for $kind"
        }
    }
    activeSheet =
        AppSheetState(
            kind = kind,
            worktreePath = normalizedWorktreePath,
        )
    publishState()
}

internal fun AppStoreCore.onDismissSheet() {
    if (activeSheet == null) {
        return
    }
    activeSheet = null
    publishState()
}
