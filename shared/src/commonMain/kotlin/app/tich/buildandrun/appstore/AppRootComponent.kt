package app.tich.buildandrun.appstore

import com.arkivanov.decompose.value.Value

class AppRootComponent internal constructor(
    private val runtime: AppRuntime,
    private val stateFeature: AppStateFeature,
    val navigation: AppNavigationFeature,
    val repositories: AppRepositoriesFeature,
    val worktrees: AppWorktreesFeature,
    val settings: AppSettingsFeature,
    val gitActions: AppGitActionsFeature,
    val editors: AppEditorsFeature,
    val kanban: AppKanbanFeature,
    val sidebar: AppSidebarFeature,
    val groups: AppGroupsFeature,
    val messages: AppMessagesFeature,
    val texts: AppTextsFeature,
    val sidebarLabels: SidebarLabels,
    val kanbanLabels: KanbanLabels,
    private val onDestroy: (() -> Unit)? = null,
) {
    val state: Value<AppStore.State> = stateFeature.state

    fun destroy() {
        (stateFeature as? AppStateService)?.destroy()
        runtime.destroy()
        onDestroy?.invoke()
    }
}
