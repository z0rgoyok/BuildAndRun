package app.tich.buildandrun.presentation.app.core

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.repositories.usecase.LoadRepositoriesUseCase
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper
import app.tich.buildandrun.presentation.app.context.state.*
import app.tich.buildandrun.resources.Res
import app.tich.buildandrun.resources.loading_initial
import kotlinx.coroutines.launch

class AppBootstrapper(
    private val executionScope: AppExecutionScope,
    private val loadingRunner: AppLoadingRunner,
    private val stateRefresher: AppStateRefresher,
    private val errorMapper: AppErrorStateMapper,
    private val repositoriesState: RepositoriesContextState,
    private val worktreesState: WorktreesContextState,
    private val settingsState: SettingsContextState,
    private val editorsState: EditorsContextState,
    private val kanbanState: KanbanContextState,
    private val messagesState: MessagesContextState,
    private val preferencesStore: PreferencesStore,
    private val loadRepositoriesUseCase: LoadRepositoriesUseCase,
    private val editorOpening: EditorOpening,
    private val loadWorktreesForRepositoryInternal: suspend (String) -> Unit,
) {
    fun start() {
        executionScope.scope.launch {
            runCatching {
                loadingRunner.withGlobalLoading(Res.string.loading_initial) {
                    when (val result = loadRepositoriesUseCase.execute()) {
                        is UseCaseResult.Success -> {
                            repositoriesState.repositories = result.value
                            repositoriesState.repositoryGroups = preferencesStore.loadRepositoryGroups()
                            repositoriesState.expandedRepositoryIds =
                                preferencesStore.expandedRepositoryIds.filter { repositoryId ->
                                    repositoriesState.repositories.any { repository -> repository.id.value == repositoryId }
                                }.toSet()
                            repositoriesState.collapsedGroupIds =
                                preferencesStore.collapsedGroupIds.filter { groupId ->
                                    repositoriesState.repositoryGroups.any { group -> group.id.value == groupId }
                                }.toSet()
                            repositoriesState.selectedRepositoryId =
                                preferencesStore.lastSelectedRepositoryId
                                    ?.takeIf { selectedId ->
                                        repositoriesState.repositories.any { it.id.value == selectedId }
                                    }
                                    ?: stateRefresher.preferredSelectedRepositoryId()
                            repositoriesState.repositories.forEach { repository ->
                                val persistedTasks = preferencesStore.loadKanbanTasks(forRepositoryId = repository.id)
                                if (persistedTasks.isNotEmpty()) {
                                    kanbanState.tasksByScope[repositoryScopeKey(repositoryId = repository.id.value)] =
                                        persistedTasks.toMutableList()
                                }
                            }
                            worktreesState.selectedWorktreePath = preferencesStore.lastSelectedWorktreePath
                            settingsState.worktreeBasePath = preferencesStore.worktreeBasePath
                            settingsState.defaultCopyPatterns = preferencesStore.defaultCopyPatterns
                            editorsState.rememberEditorChoice = preferencesStore.rememberEditorChoice
                            editorsState.enabledEditorIds = preferencesStore.enabledEditorIds
                        }

                        is UseCaseResult.Failure -> {
                            messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                        }
                    }
                    stateRefresher.refreshInstalledEditors(editorOpening = editorOpening)
                    repositoriesState.repositories.forEach { repository ->
                        loadWorktreesForRepositoryInternal(repository.path)
                    }
                    if (worktreesState.selectedWorktreePath != null) {
                        val restoredPath = worktreesState.selectedWorktreePath
                        val hasRestoredPath =
                            repositoriesState.repositories.any { repository ->
                                worktreesState.worktreesByRepositoryPath[repository.path].orEmpty().any { it.path == restoredPath }
                            }
                        if (!hasRestoredPath) {
                            worktreesState.selectedWorktreePath = null
                        }
                    }
                    stateRefresher.persistSelection()
                }
            }.onFailure { throwable ->
                messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
                stateRefresher.publishAll()
            }
        }
    }
}
