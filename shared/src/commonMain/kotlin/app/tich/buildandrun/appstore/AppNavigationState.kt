package app.tich.buildandrun.appstore

data class AppNavigationState(
    val activeChild: AppChild = AppChild.WORKSPACE,
    val activeSheet: AppSheetState? = null,
)
