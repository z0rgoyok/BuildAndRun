package app.tich.buildandrun.appstore

import app.tich.buildandrun.domain.entities.*
import app.tich.buildandrun.domain.failures.DomainFailureMapper
import app.tich.buildandrun.presentation.errors.DomainFailureToUiErrorMapper
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

internal class AppRuntime(
    internal val graph: AppStoreGraph,
) {
    internal val failureToUiErrorMapper = DomainFailureToUiErrorMapper()
    internal val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            publishState()
        }
    internal val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + coroutineExceptionHandler)
    internal val mutableState = MutableValue(AppStore.State())

    internal var repositories: List<Repository> = emptyList()
    internal var repositoryGroups: List<RepositoryGroup> = emptyList()
    internal val worktreesByRepositoryPath = mutableMapOf<String, List<Worktree>>()
    internal val worktreeStatusByPath = mutableMapOf<String, WorktreeStatus>()
    internal val worktreeStatusLoadingPaths = mutableSetOf<String>()
    internal val hasRemoteBranchByWorktreePath = mutableMapOf<String, Boolean>()
    internal val tasksByScope = mutableMapOf<String, MutableList<KanbanTask>>()

    internal val activityCenter = ActivityCenter()
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
    internal var expandedRepositoryIds: Set<String> = graph.preferencesStore.expandedRepositoryIds
    internal var collapsedGroupIds: Set<String> = graph.preferencesStore.collapsedGroupIds
    internal var error: AppStore.ErrorState? = null
    internal var success: AppStore.SuccessState? = null

    internal val state: Value<AppStore.State> = mutableState

    init {
        loadInitial()
    }
}
