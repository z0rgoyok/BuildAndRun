package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.entities.KanbanTask
import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.entities.Worktree
import app.tich.buildandrun.domain.entities.WorktreeStatus
import app.tich.buildandrun.presentation.errors.DomainFailureToUiErrorMapper
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

internal class MacOSAppStoreCore {
    internal val graph = MacOSAppGraph()
    internal val failureToUiErrorMapper = DomainFailureToUiErrorMapper()
    internal val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    internal val mutableState = MutableValue(MacOSAppStore.State())

    internal var repositories: List<Repository> = emptyList()
    internal val worktreesByRepositoryPath = mutableMapOf<String, List<Worktree>>()
    internal val worktreeStatusByPath = mutableMapOf<String, WorktreeStatus>()
    internal val worktreeStatusLoadingPaths = mutableSetOf<String>()
    internal val tasksByScope = mutableMapOf<String, MutableList<KanbanTask>>()

    internal var isLoading: Boolean = false
    internal var addRepositoryPathInput: String = ""
    internal var createWorktreeState = MacOSAppStore.CreateWorktreeState()
    internal var selectedRepositoryId: String? = null
    internal var selectedWorktreePath: String? = null
    internal var error: MacOSAppStore.ErrorState? = null
    internal var success: MacOSAppStore.SuccessState? = null

    internal val state: Value<MacOSAppStore.State> = mutableState

    init {
        loadInitial()
    }
}
