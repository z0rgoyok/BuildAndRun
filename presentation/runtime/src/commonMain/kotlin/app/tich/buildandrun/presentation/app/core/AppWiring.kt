package app.tich.buildandrun.presentation.app.core

import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper
import app.tich.buildandrun.presentation.app.*
import app.tich.buildandrun.presentation.app.context.state.*
import app.tich.buildandrun.presentation.errors.DomainFailureToUiErrorMapper
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppWiring(
    val graph: AppGraph,
) {
    val failureToUiErrorMapper = DomainFailureToUiErrorMapper()
    val repositoriesState = RepositoriesContextState(preferencesStore = graph.preferencesStore)
    val worktreesState = WorktreesContextState()
    val settingsState = SettingsContextState(preferencesStore = graph.preferencesStore)
    val editorsState =
        EditorsContextState(
            preferencesStore = graph.preferencesStore,
            editorOpening = graph.editorOpening,
        )
    val kanbanState = KanbanContextState()
    val messagesState = MessagesContextState()
    val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            publishState()
        }
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + coroutineExceptionHandler)
    val mutableActivityState = MutableValue(ActivityState())
    val mutableRepositoriesState = MutableValue(RepositoriesState())
    val mutableWorktreesState = MutableValue(WorktreesState())
    val mutableSettingsState = MutableValue(SettingsState())
    val mutableEditorsState = MutableValue(EditorsState())
    val mutableKanbanState = MutableValue(KanbanState())
    val mutableMessagesState = MutableValue(MessagesState())
    val activityCenter = ActivityCenter()
    val activityState: Value<ActivityState> = mutableActivityState
    val repositoriesUiState: Value<RepositoriesState> = mutableRepositoriesState
    val worktreesUiState: Value<WorktreesState> = mutableWorktreesState
    val settingsUiState: Value<SettingsState> = mutableSettingsState
    val editorsUiState: Value<EditorsState> = mutableEditorsState
    val kanbanUiState: Value<KanbanState> = mutableKanbanState
    val messagesUiState: Value<MessagesState> = mutableMessagesState

    init {
        loadInitial()
    }
}
