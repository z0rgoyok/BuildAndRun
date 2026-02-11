package app.tich.buildandrun.presentation.app.context.repositories.impl

import app.tich.buildandrun.application.context.repositories.usecase.*
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.worktrees.usecase.LoadRepositoryWorktreesUseCase
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.presentation.app.AppRepositoriesFeature
import app.tich.buildandrun.presentation.app.SuccessState
import app.tich.buildandrun.presentation.app.context.state.*
import app.tich.buildandrun.presentation.app.core.*
import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.resources.*
import kotlinx.coroutines.launch

class AppRepositoriesService(
    private val executionScope: AppExecutionScope,
    private val loadingRunner: AppLoadingRunner,
    private val stateRefresher: AppStateRefresher,
    private val errorMapper: AppErrorStateMapper,
    private val repositoriesState: RepositoriesContextState,
    private val worktreesState: WorktreesContextState,
    private val kanbanState: KanbanContextState,
    private val messagesState: MessagesContextState,
    private val addRepositoryUseCase: AddRepositoryUseCase,
    private val removeRepositoryUseCase: RemoveRepositoryUseCase,
    private val setRepositoryArchivedStateUseCase: SetRepositoryArchivedStateUseCase,
    private val appSessionPersistenceUseCase: AppSessionPersistenceUseCase,
    private val clearKanbanTasksUseCase: ClearKanbanTasksUseCase,
    private val loadRepositoryWorktreesUseCase: LoadRepositoryWorktreesUseCase,
) : AppRepositoriesFeature {
    override fun onAddRepositoryPathChanged(value: String) {
        repositoriesState.addRepositoryPathInput = value
        messagesState.clear()
        stateRefresher.publishAll()
    }

    override fun onAddRepository() {
        if (stateRefresher.isGlobalActive()) {
            return
        }
        executionScope.scope.launch {
            loadingRunner.withGlobalLoading(Res.string.loading_adding_repository) {
                when (
                    val result =
                        addRepositoryUseCase.execute(
                            input = AddRepositoryUseCase.Input(path = repositoriesState.addRepositoryPathInput),
                        )
                ) {
                    is UseCaseResult.Success -> {
                        repositoriesState.repositories = result.value.repositories
                        repositoriesState.selectedRepositoryId = result.value.addedRepository.id.value
                        worktreesState.selectedWorktreePath = result.value.worktrees.firstOrNull()?.path
                        worktreesState.worktreesByRepositoryPath[result.value.addedRepository.path] = result.value.worktrees
                        repositoriesState.addRepositoryPathInput = ""
                        worktreesState.createWorktreeState =
                            worktreesState.createWorktreeState.copy(
                                branchInput = "",
                                worktreePathInput = "",
                                baseBranchInput = "",
                                createBranch = true,
                                isSubmitting = false,
                                createdWorktreePath = null,
                            )
                        messagesState.success =
                            SuccessState(
                                message =
                                    resolveText(
                                        text =
                                            UiText(
                                                resource = Res.string.screen_repositories_repository_added,
                                                args = listOf(result.value.addedRepository.name),
                                            ),
                                    ),
                            )
                        persistSelection()
                    }

                    is UseCaseResult.Failure -> {
                        messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                    }
                }
            }
        }
    }

    override fun onSelectRepository(repositoryId: String) {
        if (repositoriesState.selectedRepositoryId == repositoryId && worktreesState.selectedWorktreePath == null) {
            return
        }
        val repositoryChanged = repositoriesState.selectedRepositoryId != repositoryId
        repositoriesState.selectedRepositoryId = repositoryId
        worktreesState.selectedWorktreePath = null
        persistSelection()
        messagesState.clear()
        stateRefresher.publishAll()
        if (repositoryChanged) {
            val selectedRepository = repositoriesState.repositories.firstOrNull { it.id.value == repositoryId } ?: return
            loadWorktreesForRepository(path = selectedRepository.path)
        }
    }

    override fun onArchiveRepository(repositoryId: String) {
        onSetRepositoryArchivedState(
            repositoryId = repositoryId,
            isArchived = true,
        )
    }

    override fun onRestoreRepository(repositoryId: String) {
        onSetRepositoryArchivedState(
            repositoryId = repositoryId,
            isArchived = false,
        )
    }

    override fun onRemoveRepository(repositoryId: String) {
        if (stateRefresher.isGlobalActive()) {
            return
        }
        executionScope.scope.launch {
            loadingRunner.withGlobalLoading(Res.string.loading_removing_repository) {
                when (
                    val result =
                        removeRepositoryUseCase.execute(
                            input = RemoveRepositoryUseCase.Input(repositoryId = repositoryId),
                        )
                ) {
                    is UseCaseResult.Success -> {
                        repositoriesState.repositories = result.value.repositories
                        cleanupRepositoryData(repository = result.value.removedRepository)
                        if (repositoriesState.selectedRepositoryId == result.value.removedRepository.id.value) {
                            repositoriesState.selectedRepositoryId = stateRefresher.preferredSelectedRepositoryId()
                            worktreesState.selectedWorktreePath = null
                        }
                        persistSelection()
                    }

                    is UseCaseResult.Failure -> {
                        messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                    }
                }
            }
        }
    }

    private fun onSetRepositoryArchivedState(
        repositoryId: String,
        isArchived: Boolean,
    ) {
        if (stateRefresher.isGlobalActive()) {
            return
        }
        val resource = if (isArchived) Res.string.loading_archiving else Res.string.loading_restoring
        executionScope.scope.launch {
            loadingRunner.withGlobalLoading(resource) {
                when (
                    val result =
                        setRepositoryArchivedStateUseCase.execute(
                            input =
                                SetRepositoryArchivedStateUseCase.Input(
                                    repositoryId = repositoryId,
                                    isArchived = isArchived,
                                ),
                        )
                ) {
                    is UseCaseResult.Success -> {
                        repositoriesState.repositories = result.value.repositories
                        if (
                            repositoriesState.selectedRepositoryId == result.value.updatedRepository.id.value &&
                            result.value.updatedRepository.isArchived
                        ) {
                            val fallbackRepository = repositoriesState.preferredSelectedRepository()
                            if (fallbackRepository?.id?.value != result.value.updatedRepository.id.value) {
                                repositoriesState.selectedRepositoryId = fallbackRepository?.id?.value
                                worktreesState.selectedWorktreePath = null
                            }
                        }
                        persistSelection()
                    }

                    is UseCaseResult.Failure -> {
                        messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                    }
                }
            }
        }
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

    private fun cleanupRepositoryData(repository: Repository) {
        val removedWorktreePaths = worktreesState.worktreesByRepositoryPath.remove(repository.path).orEmpty().map { it.path }
        kanbanState.tasksByScope.remove(repositoryScopeKey(repositoryId = repository.id.value))
        when (
            val result =
                clearKanbanTasksUseCase.execute(
                    input = ClearKanbanTasksUseCase.Input(repositoryId = repository.id.value),
                )
        ) {
            is UseCaseResult.Success -> {
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
        removedWorktreePaths.forEach { worktreesState.worktreeStatusByPath.remove(it) }
        removedWorktreePaths.forEach { worktreesState.hasRemoteBranchByWorktreePath.remove(it) }
        worktreesState.worktreeStatusLoadingPaths.removeAll(removedWorktreePaths.toSet())
    }

    private fun loadWorktreesForRepository(path: String) {
        executionScope.scope.launch {
            loadingRunner.withGlobalLoading(Res.string.loading_refreshing) {
                when (
                    val result =
                        loadRepositoryWorktreesUseCase.execute(
                            input = LoadRepositoryWorktreesUseCase.Input(repositoryPath = path),
                        )
                ) {
                    is UseCaseResult.Success -> {
                        worktreesState.worktreesByRepositoryPath[path] = result.value.worktrees
                    }

                    is UseCaseResult.Failure -> {
                        messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                    }
                }
            }
            stateRefresher.publishAll()
        }
    }
}
