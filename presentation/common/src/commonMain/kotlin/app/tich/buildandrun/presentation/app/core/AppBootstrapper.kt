package app.tich.buildandrun.presentation.app.core

import app.tich.buildandrun.application.context.repositories.usecase.AppSessionPersistenceUseCase
import app.tich.buildandrun.application.context.repositories.usecase.RestoreAppSessionUseCase
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.application.context.worktrees.usecase.LoadRepositoryWorktreesUseCase
import app.tich.buildandrun.application.context.worktrees.usecase.LoadWorktreeStatusUseCase
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
    private val restoreAppSessionUseCase: RestoreAppSessionUseCase,
    private val appSessionPersistenceUseCase: AppSessionPersistenceUseCase,
    private val loadRepositoryWorktreesUseCase: LoadRepositoryWorktreesUseCase,
    private val loadWorktreeStatusUseCase: LoadWorktreeStatusUseCase,
) {
    fun start() {
        executionScope.scope.launch {
            runCatchingCancellable {
                loadingRunner.withGlobalLoading(Res.string.loading_initial) {
                    when (val result = restoreAppSessionUseCase.execute()) {
                        is UseCaseResult.Success -> {
                            applyRestoredState(output = result.value)
                        }

                        is UseCaseResult.Failure -> {
                            messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                            return@withGlobalLoading
                        }
                    }
                    stateRefresher.refreshInstalledEditors()
                    repositoriesState.repositories.forEach { repository ->
                        loadWorktreesForRepository(path = repository.path)
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
                    persistSelection()
                }
            }.onFailure { throwable ->
                messagesState.error = errorMapper.mapFailureToErrorState(throwable.toUseCaseFailure().value)
                stateRefresher.publishAll()
            }
        }
    }

    private fun applyRestoredState(output: RestoreAppSessionUseCase.Output) {
        repositoriesState.repositories = output.repositories
        repositoriesState.repositoryGroups = output.repositoryGroups
        repositoriesState.expandedRepositoryIds = output.expandedRepositoryIds
        repositoriesState.collapsedGroupIds = output.collapsedGroupIds
        repositoriesState.selectedRepositoryId = output.selectedRepositoryId ?: stateRefresher.preferredSelectedRepositoryId()
        kanbanState.tasksByScope.clear()
        repositoriesState.repositories.forEach { repository ->
            val persistedTasks = output.kanbanTasksByRepositoryId[repository.id.value].orEmpty()
            if (persistedTasks.isNotEmpty()) {
                kanbanState.tasksByScope[repositoryScopeKey(repositoryId = repository.id.value)] = persistedTasks.toMutableList()
            }
        }
        worktreesState.selectedWorktreePath = output.selectedWorktreePath
        settingsState.worktreeBasePath = output.worktreeBasePath
        settingsState.defaultCopyPatterns = output.defaultCopyPatterns
        editorsState.rememberEditorChoice = output.rememberEditorChoice
        editorsState.enabledEditorIds = output.enabledEditorIds
    }

    private fun persistSelection() {
        when (
            val result =
                appSessionPersistenceUseCase.execute(
                    input =
                        AppSessionPersistenceUseCase.Input(
                            repositoryId = repositoriesState.selectedRepositoryId,
                            worktreePath = worktreesState.selectedWorktreePath,
                        ),
                )
        ) {
            is UseCaseResult.Success -> {
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
    }

    private suspend fun loadWorktreesForRepository(path: String) {
        when (
            val result =
                loadRepositoryWorktreesUseCase.execute(
                    input = LoadRepositoryWorktreesUseCase.Input(repositoryPath = path),
                )
        ) {
            is UseCaseResult.Success -> {
                worktreesState.worktreesByRepositoryPath[path] = result.value.worktrees
                result.value.worktrees.forEach { worktree ->
                    when (
                        val statusResult =
                            loadWorktreeStatusUseCase.execute(
                                input = LoadWorktreeStatusUseCase.Input(worktreePath = worktree.path),
                            )
                    ) {
                        is UseCaseResult.Success -> {
                            worktreesState.worktreeStatusByPath[worktree.path] = statusResult.value.status
                        }

                        is UseCaseResult.Failure -> {
                            messagesState.error = errorMapper.mapFailureToErrorState(statusResult.value)
                        }
                    }
                }
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
    }
}
