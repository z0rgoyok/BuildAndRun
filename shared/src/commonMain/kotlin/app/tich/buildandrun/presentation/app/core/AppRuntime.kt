package app.tich.buildandrun.presentation.app.core

import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper
import app.tich.buildandrun.presentation.app.AppStore
import app.tich.buildandrun.presentation.app.context.state.*
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
    internal val repositoriesState = RepositoriesContextState(preferencesStore = graph.preferencesStore)
    internal val worktreesState = WorktreesContextState()
    internal val settingsState = SettingsContextState(preferencesStore = graph.preferencesStore)
    internal val editorsState =
        EditorsContextState(
            preferencesStore = graph.preferencesStore,
            editorOpening = graph.editorOpening,
        )
    internal val kanbanState = KanbanContextState()
    internal val messagesState = MessagesContextState()
    internal val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            publishState()
        }
    internal val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + coroutineExceptionHandler)
    internal val mutableState = MutableValue(AppStore.State())
    internal val activityCenter = ActivityCenter()

    internal val state: Value<AppStore.State> = mutableState

    init {
        loadInitial()
    }
}
