package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.failures.DomainFailureMapper
import app.tich.buildandrun.domain.usecases.CreateWorktreeUseCase
import app.tich.buildandrun.domain.usecases.UseCaseResult
import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.resources.Res
import app.tich.buildandrun.resources.screen_create_worktree_success
import kotlinx.coroutines.launch

internal fun MacOSAppStoreCore.onSelectWorktree(worktreePath: String?) {
    selectedWorktreePath = worktreePath
    clearMessages()
    publishState()
}

internal fun MacOSAppStoreCore.onRefreshSelectedRepository() {
    val repositoryPath = selectedRepository()?.path ?: return
    clearMessages()
    loadWorktreesForRepository(path = repositoryPath)
}

internal fun MacOSAppStoreCore.onCreateWorktreeBranchChanged(value: String) {
    val selectedRepositoryPath = selectedRepository()?.path.orEmpty()
    val normalizedBranch = value.trim()
    val currentWorktreePath = createWorktreeState.worktreePathInput
    val updatedWorktreePath =
        if (currentWorktreePath.isBlank() && normalizedBranch.isNotBlank() && selectedRepositoryPath.isNotBlank()) {
            suggestWorktreePath(repositoryPath = selectedRepositoryPath, branch = normalizedBranch)
        } else {
            currentWorktreePath
        }
    createWorktreeState =
        createWorktreeState.copy(
            branchInput = value,
            worktreePathInput = updatedWorktreePath,
            createdWorktreePath = null,
        )
    clearMessages()
    publishState()
}

internal fun MacOSAppStoreCore.onCreateWorktreePathChanged(value: String) {
    createWorktreeState =
        createWorktreeState.copy(
            worktreePathInput = value,
            createdWorktreePath = null,
        )
    clearMessages()
    publishState()
}

internal fun MacOSAppStoreCore.onCreateWorktreeBaseBranchChanged(value: String) {
    createWorktreeState = createWorktreeState.copy(baseBranchInput = value)
    clearMessages()
    publishState()
}

internal fun MacOSAppStoreCore.onCreateWorktreeCreateBranchChanged(value: Boolean) {
    createWorktreeState = createWorktreeState.copy(createBranch = value)
    clearMessages()
    publishState()
}

internal fun MacOSAppStoreCore.onCreateWorktree() {
    if (createWorktreeState.isSubmitting || isLoading) {
        return
    }
    val repositoryPath = selectedRepository()?.path ?: return
    scope.launch {
        createWorktreeState =
            createWorktreeState.copy(
                isSubmitting = true,
                createdWorktreePath = null,
            )
        clearMessages()
        publishState()
        when (
            val result =
                graph.createWorktreeUseCase.execute(
                    input =
                        CreateWorktreeUseCase.Input(
                            repositoryPath = repositoryPath,
                            branch = createWorktreeState.branchInput,
                            worktreePath = createWorktreeState.worktreePathInput,
                            createBranch = createWorktreeState.createBranch,
                            baseBranch = createWorktreeState.baseBranchInput,
                        ),
                )
        ) {
            is UseCaseResult.Success -> {
                val worktrees = result.value.allWorktrees
                worktreesByRepositoryPath[repositoryPath] = worktrees
                selectedWorktreePath = result.value.createdWorktree.path
                createWorktreeState =
                    createWorktreeState.copy(
                        isSubmitting = false,
                        createdWorktreePath = result.value.createdWorktree.path,
                    )
                success =
                    MacOSAppStore.SuccessState(
                        message =
                            resolveText(
                                text =
                                    UiText(
                                        resource = Res.string.screen_create_worktree_success,
                                        args = listOf(result.value.createdWorktree.name),
                                    ),
                            ),
                    )
            }

            is UseCaseResult.Failure -> {
                createWorktreeState = createWorktreeState.copy(isSubmitting = false)
                error = mapFailureToErrorState(result.value)
            }
        }
        publishState()
    }
}

internal fun MacOSAppStoreCore.loadWorktreesForRepository(path: String) {
    scope.launch {
        isLoading = true
        publishState()
        loadWorktreesForRepositoryInternal(path = path)
        isLoading = false
        publishState()
    }
}

internal suspend fun MacOSAppStoreCore.loadWorktreesForRepositoryInternal(path: String) {
    val normalizedPath = normalizePath(path)
    if (normalizedPath.isBlank()) {
        return
    }
    runCatching {
        graph.gitClient.listWorktrees(atRepoPath = normalizedPath)
    }.onSuccess { worktrees ->
        worktreesByRepositoryPath[normalizedPath] = worktrees
        if (selectedRepository()?.path == normalizedPath && selectedWorktreePath != null) {
            val stillExists = worktrees.any { it.path == selectedWorktreePath }
            if (!stillExists) {
                selectedWorktreePath = null
            }
        }
        worktrees.forEach { worktree ->
            if (!worktreeStatusByPath.containsKey(worktree.path)) {
                onRefreshWorktreeStatus(worktreePath = worktree.path)
            }
        }
    }.onFailure { throwable ->
        val domainFailure = DomainFailureMapper.fromThrowable(throwable)
        error = mapFailureToErrorState(domainFailure)
    }
}
