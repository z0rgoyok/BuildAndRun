package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.usecases.AddRepositoryUseCase
import app.tich.buildandrun.domain.usecases.RemoveRepositoryUseCase
import app.tich.buildandrun.domain.usecases.SetRepositoryArchivedStateUseCase
import app.tich.buildandrun.domain.usecases.UseCaseResult
import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.resources.Res
import app.tich.buildandrun.resources.screen_repositories_repository_added
import kotlinx.coroutines.launch

internal fun MacOSAppStoreCore.onAddRepositoryPathChanged(value: String) {
    addRepositoryPathInput = value
    clearMessages()
    publishState()
}

internal fun MacOSAppStoreCore.onAddRepository() {
    if (isLoading) {
        return
    }
    scope.launch {
        isLoading = true
        clearMessages()
        publishState()
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
                    MacOSAppStore.SuccessState(
                        message =
                            resolveText(
                                text =
                                    UiText(
                                        resource = Res.string.screen_repositories_repository_added,
                                        args = listOf(result.value.addedRepository.name),
                                    ),
                            ),
                    )
            }

            is UseCaseResult.Failure -> {
                error = mapFailureToErrorState(result.value)
            }
        }
        isLoading = false
        publishState()
    }
}

internal fun MacOSAppStoreCore.onSelectRepository(repositoryId: String) {
    if (selectedRepositoryId == repositoryId) {
        return
    }
    selectedRepositoryId = repositoryId
    selectedWorktreePath = null
    clearMessages()
    publishState()
    val selectedRepository = repositories.firstOrNull { it.id.value == repositoryId } ?: return
    loadWorktreesForRepository(path = selectedRepository.path)
}

internal fun MacOSAppStoreCore.onArchiveRepository(repositoryId: String) {
    onSetRepositoryArchivedState(
        repositoryId = repositoryId,
        isArchived = true,
    )
}

internal fun MacOSAppStoreCore.onRestoreRepository(repositoryId: String) {
    onSetRepositoryArchivedState(
        repositoryId = repositoryId,
        isArchived = false,
    )
}

internal fun MacOSAppStoreCore.onRemoveRepository(repositoryId: String) {
    if (isLoading) {
        return
    }
    scope.launch {
        isLoading = true
        clearMessages()
        publishState()
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
            }

            is UseCaseResult.Failure -> {
                error = mapFailureToErrorState(result.value)
            }
        }
        isLoading = false
        publishState()
    }
}

internal fun MacOSAppStoreCore.onSetRepositoryArchivedState(
    repositoryId: String,
    isArchived: Boolean,
) {
    if (isLoading) {
        return
    }
    scope.launch {
        isLoading = true
        clearMessages()
        publishState()
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
            }

            is UseCaseResult.Failure -> {
                error = mapFailureToErrorState(result.value)
            }
        }
        isLoading = false
        publishState()
    }
}
