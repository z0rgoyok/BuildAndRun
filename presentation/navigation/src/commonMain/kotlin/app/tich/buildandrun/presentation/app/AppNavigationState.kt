package app.tich.buildandrun.presentation.app

data class AppNavigationState(
    val activeChild: AppChild = AppChild.WORKSPACE,
    val activeSheet: AppSheetState? = null,
)
