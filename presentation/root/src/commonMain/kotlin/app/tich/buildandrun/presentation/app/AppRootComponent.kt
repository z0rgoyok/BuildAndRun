package app.tich.buildandrun.presentation.app

import app.tich.buildandrun.presentation.app.core.AppWiring
import app.tich.buildandrun.presentation.app.core.destroy
import com.arkivanov.decompose.value.Value

class AppRootComponent internal constructor(
    private val runtime: AppWiring,
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
    val navigationState: Value<AppNavigationState> = navigation.state
    val activityState: Value<ActivityState> = runtime.activityState
    val repositoriesState: Value<RepositoriesState> = runtime.repositoriesUiState
    val worktreesState: Value<WorktreesState> = runtime.worktreesUiState
    val settingsState: Value<SettingsState> = runtime.settingsUiState
    val editorsState: Value<EditorsState> = runtime.editorsUiState
    val kanbanState: Value<KanbanState> = runtime.kanbanUiState
    val messagesState: Value<MessagesState> = runtime.messagesUiState

    fun destroy() {
        navigationState
        activityState
        settingsState
        editorsState
        gitActions
        groups
        messages
        texts
        sidebarLabels
        runtime.destroy()
        onDestroy?.invoke()
    }
}
