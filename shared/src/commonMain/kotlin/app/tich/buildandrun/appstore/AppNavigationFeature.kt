package app.tich.buildandrun.appstore

import com.arkivanov.decompose.value.Value

interface AppNavigationFeature {
    val state: Value<AppNavigationState>

    fun onSelectChild(child: AppChild)

    fun onPresentSheet(
        kind: AppSheetKind,
        worktreePath: String?,
    )

    fun onDismissSheet()
}
