package app.tich.buildandrun.presentation.app.context.repositories.impl

import app.tich.buildandrun.application.context.repositories.usecase.AddRepositoryUseCase
import app.tich.buildandrun.application.context.repositories.usecase.RemoveRepositoryUseCase
import app.tich.buildandrun.application.context.repositories.usecase.SetRepositoryArchivedStateUseCase
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.presentation.app.SuccessState
import app.tich.buildandrun.presentation.app.context.worktrees.impl.loadWorktreesForRepository
import app.tich.buildandrun.presentation.app.core.*
import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.resources.*
import kotlinx.coroutines.launch

internal fun AppRuntime.onAddRepositoryPathChanged(value: String) {
    repositoriesState.addRepositoryPathInput = value
    clearMessages()
    publishState()
}

internal fun AppRuntime.onAddRepository() {
    if (activityCenter.isGlobalActive) {
        return
    }
    scope.launch {
        withGlobalLoading(Res.string.loading_adding_repository) {
            when (
                val result =
                    graph.addRepositoryUseCase.execute(
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
                    messagesState.error = mapFailureToErrorState(result.value)
                }
            }
        }
    }
}

internal fun AppRuntime.onSelectRepository(repositoryId: String) {
    if (repositoriesState.selectedRepositoryId == repositoryId && worktreesState.selectedWorktreePath == null) {
        return
    }
    val repositoryChanged = repositoriesState.selectedRepositoryId != repositoryId
    repositoriesState.selectedRepositoryId = repositoryId
    worktreesState.selectedWorktreePath = null
    persistSelection()
    clearMessages()
    publishState()
    if (repositoryChanged) {
        val selectedRepository = repositoriesState.repositories.firstOrNull { it.id.value == repositoryId } ?: return
        loadWorktreesForRepository(path = selectedRepository.path)
    }
}

internal fun AppRuntime.onArchiveRepository(repositoryId: String) {
    onSetRepositoryArchivedState(
        repositoryId = repositoryId,
        isArchived = true,
    )
}

internal fun AppRuntime.onRestoreRepository(repositoryId: String) {
    onSetRepositoryArchivedState(
        repositoryId = repositoryId,
        isArchived = false,
    )
}

internal fun AppRuntime.onRemoveRepository(repositoryId: String) {
    if (activityCenter.isGlobalActive) {
        return
    }
    scope.launch {
        withGlobalLoading(Res.string.loading_removing_repository) {
            when (
                val result =
                    graph.removeRepositoryUseCase.execute(
                        input = RemoveRepositoryUseCase.Input(repositoryId = repositoryId),
                    )
            ) {
                is UseCaseResult.Success -> {
                    repositoriesState.repositories = result.value.repositories
                    cleanupRepositoryData(repository = result.value.removedRepository)
                    if (repositoriesState.selectedRepositoryId == result.value.removedRepository.id.value) {
                        repositoriesState.selectedRepositoryId = preferredSelectedRepositoryId()
                        worktreesState.selectedWorktreePath = null
                    }
                    persistSelection()
                }

                is UseCaseResult.Failure -> {
                    messagesState.error = mapFailureToErrorState(result.value)
                }
            }
        }
    }
}

internal fun AppRuntime.onSetRepositoryArchivedState(
    repositoryId: String,
    isArchived: Boolean,
) {
    if (activityCenter.isGlobalActive) {
        return
    }
    val resource = if (isArchived) Res.string.loading_archiving else Res.string.loading_restoring
    scope.launch {
        withGlobalLoading(resource) {
            when (
                val result =
                    graph.setRepositoryArchivedStateUseCase.execute(
                        input =
                            SetRepositoryArchivedStateUseCase.Input(
                                repositoryId = repositoryId,
                                isArchived = isArchived,
                            ),
                    )
            ) {
                is UseCaseResult.Success -> {
                    repositoriesState.repositories = result.value.repositories
                    if (repositoriesState.selectedRepositoryId == result.value.updatedRepository.id.value && result.value.updatedRepository.isArchived) {
                        val fallbackRepository = preferredSelectedRepository()
                        if (fallbackRepository?.id?.value != result.value.updatedRepository.id.value) {
                            repositoriesState.selectedRepositoryId = fallbackRepository?.id?.value
                            worktreesState.selectedWorktreePath = null
                        }
                    }
                    persistSelection()
                }

                is UseCaseResult.Failure -> {
                    messagesState.error = mapFailureToErrorState(result.value)
                }
            }
        }
    }
}
