package app.tich.buildandrun.appstore

import app.tich.buildandrun.domain.entities.*
import app.tich.buildandrun.presentation.errors.DomainFailureToUiErrorMapper
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

internal class AppStoreCore(
    internal val graph: AppStoreGraph,
) {
    internal val failureToUiErrorMapper = DomainFailureToUiErrorMapper()
    internal val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    internal val mutableState = MutableValue(AppStore.State())

    internal var repositories: List<Repository> = emptyList()
    internal val worktreesByRepositoryPath = mutableMapOf<String, List<Worktree>>()
    internal val worktreeStatusByPath = mutableMapOf<String, WorktreeStatus>()
    internal val worktreeStatusLoadingPaths = mutableSetOf<String>()
    internal val hasRemoteBranchByWorktreePath = mutableMapOf<String, Boolean>()
    internal val tasksByScope = mutableMapOf<String, MutableList<KanbanTask>>()

    internal var isLoading: Boolean = false
    internal var addRepositoryPathInput: String = ""
    internal var branches: List<String> = emptyList()
    internal var worktreeBasePath: String = graph.preferencesStore.worktreeBasePath
    internal var defaultCopyPatterns: List<CopyPattern> = graph.preferencesStore.defaultCopyPatterns
    internal var rememberEditorChoice: Boolean = graph.preferencesStore.rememberEditorChoice
    internal var enabledEditorIds: Set<String>? = graph.preferencesStore.enabledEditorIds
    internal val allEditors: List<Editor> = graph.editorOpening.allEditors()
    internal val installedEditorIds: MutableSet<String> = mutableSetOf()
    internal var createWorktreeState = AppStore.CreateWorktreeState()
    internal var selectedRepositoryId: String? = null
    internal var selectedWorktreePath: String? = null
    internal var activeChild: AppChild = AppChild.WORKSPACE
    internal var activeSheet: AppSheetState? = null
    internal var error: AppStore.ErrorState? = null
    internal var success: AppStore.SuccessState? = null

    internal val state: Value<AppStore.State> = mutableState

    init {
        loadInitial()
    }
}
