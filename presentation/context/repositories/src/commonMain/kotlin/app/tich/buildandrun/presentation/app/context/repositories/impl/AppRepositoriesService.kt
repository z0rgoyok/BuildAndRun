package app.tich.buildandrun.presentation.app.context.repositories.impl

import app.tich.buildandrun.application.context.repositories.usecase.AddRepositoryUseCase
import app.tich.buildandrun.application.context.repositories.usecase.RemoveRepositoryUseCase
import app.tich.buildandrun.application.context.repositories.usecase.SetRepositoryArchivedStateUseCase
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.presentation.app.AppRepositoriesFeature
import app.tich.buildandrun.presentation.app.SuccessState
import app.tich.buildandrun.presentation.app.context.state.MessagesContextState
import app.tich.buildandrun.presentation.app.context.state.RepositoriesContextState
import app.tich.buildandrun.presentation.app.context.state.WorktreesContextState
import app.tich.buildandrun.presentation.app.context.worktrees.impl.WorktreesOperations
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
    private val messagesState: MessagesContextState,
    private val addRepositoryUseCase: AddRepositoryUseCase,
    private val removeRepositoryUseCase: RemoveRepositoryUseCase,
    private val setRepositoryArchivedStateUseCase: SetRepositoryArchivedStateUseCase,
    private val worktreesOperations: WorktreesOperations,
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
                        stateRefresher.persistSelection()
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
        stateRefresher.persistSelection()
        messagesState.clear()
        stateRefresher.publishAll()
        if (repositoryChanged) {
            val selectedRepository = repositoriesState.repositories.firstOrNull { it.id.value == repositoryId } ?: return
            worktreesOperations.loadWorktreesForRepository(path = selectedRepository.path)
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
                        stateRefresher.cleanupRepositoryData(repository = result.value.removedRepository)
                        if (repositoriesState.selectedRepositoryId == result.value.removedRepository.id.value) {
                            repositoriesState.selectedRepositoryId = stateRefresher.preferredSelectedRepositoryId()
                            worktreesState.selectedWorktreePath = null
                        }
                        stateRefresher.persistSelection()
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
                        stateRefresher.persistSelection()
                    }

                    is UseCaseResult.Failure -> {
                        messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                    }
                }
            }
        }
    }
}
