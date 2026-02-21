package app.tich.buildandrun.presentation.app

import app.tich.buildandrun.presentation.app.core.AppExecutionScope
import com.arkivanov.decompose.value.Value

class AppRootComponent internal constructor(
    private val executionScope: AppExecutionScope,
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
    val activityState: Value<ActivityState>,
    val repositoriesState: Value<RepositoriesState>,
    val worktreesState: Value<WorktreesState>,
    val settingsState: Value<SettingsState>,
    val editorsState: Value<EditorsState>,
    val kanbanState: Value<KanbanState>,
    val messagesState: Value<MessagesState>,
    private val onDestroy: (() -> Unit)? = null,
) {
    val navigationState: Value<AppNavigationState> = navigation.state

    fun destroy() {
        navigationState
        activityState
        worktreesState
        settingsState
        editorsState
        settings
        editors
        gitActions
        groups
        messages
        texts
        sidebarLabels
        kanbanLabels
        executionScope.destroy()
        onDestroy?.invoke()
    }
}
