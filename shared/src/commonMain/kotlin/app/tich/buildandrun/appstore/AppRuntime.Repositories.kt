package app.tich.buildandrun.appstore

import app.tich.buildandrun.application.usecases.AddRepositoryUseCase
import app.tich.buildandrun.application.usecases.RemoveRepositoryUseCase
import app.tich.buildandrun.application.usecases.SetRepositoryArchivedStateUseCase
import app.tich.buildandrun.application.usecases.UseCaseResult
import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.resources.*
import kotlinx.coroutines.launch

internal fun AppRuntime.onAddRepositoryPathChanged(value: String) {
    addRepositoryPathInput = value
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
                        input = AddRepositoryUseCase.Input(path = addRepositoryPathInput),
                    )
            ) {
                is UseCaseResult.Success -> {
                    repositories = result.value.repositories
                    selectedRepositoryId = result.value.addedRepository.id.value
                    selectedWorktreePath = result.value.worktrees.firstOrNull()?.path
                    worktreesByRepositoryPath[result.value.addedRepository.path] = result.value.worktrees
                    addRepositoryPathInput = ""
                    createWorktreeState =
                        createWorktreeState.copy(
                            branchInput = "",
                            worktreePathInput = "",
                            baseBranchInput = "",
                            createBranch = true,
                            isSubmitting = false,
                            createdWorktreePath = null,
                        )
                    success =
                        AppStore.SuccessState(
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
                    error = mapFailureToErrorState(result.value)
                }
            }
        }
    }
}

internal fun AppRuntime.onSelectRepository(repositoryId: String) {
    if (selectedRepositoryId == repositoryId && selectedWorktreePath == null) {
        return
    }
    val repositoryChanged = selectedRepositoryId != repositoryId
    selectedRepositoryId = repositoryId
    selectedWorktreePath = null
    persistSelection()
    clearMessages()
    publishState()
    if (repositoryChanged) {
        val selectedRepository = repositories.firstOrNull { it.id.value == repositoryId } ?: return
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
                    repositories = result.value.repositories
                    cleanupRepositoryData(repository = result.value.removedRepository)
                    if (selectedRepositoryId == result.value.removedRepository.id.value) {
                        selectedRepositoryId = preferredSelectedRepositoryId()
                        selectedWorktreePath = null
                    }
                    persistSelection()
                }

                is UseCaseResult.Failure -> {
                    error = mapFailureToErrorState(result.value)
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
                    repositories = result.value.repositories
                    if (selectedRepositoryId == result.value.updatedRepository.id.value && result.value.updatedRepository.isArchived) {
                        val fallbackRepository = preferredSelectedRepository()
                        if (fallbackRepository?.id?.value != result.value.updatedRepository.id.value) {
                            selectedRepositoryId = fallbackRepository?.id?.value
                            selectedWorktreePath = null
                        }
                    }
                    persistSelection()
                }

                is UseCaseResult.Failure -> {
                    error = mapFailureToErrorState(result.value)
                }
            }
        }
    }
}
