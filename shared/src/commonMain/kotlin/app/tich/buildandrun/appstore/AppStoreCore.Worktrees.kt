package app.tich.buildandrun.appstore

import app.tich.buildandrun.application.usecases.CreateWorktreeUseCase
import app.tich.buildandrun.application.usecases.UseCaseResult
import app.tich.buildandrun.domain.failures.DomainFailureMapper
import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.resources.Res
import app.tich.buildandrun.resources.loading_refreshing
import app.tich.buildandrun.resources.screen_create_worktree_success
import kotlinx.coroutines.launch

internal fun AppStoreCore.onSelectWorktree(worktreePath: String?) {
    selectedWorktreePath = worktreePath
    persistSelection()
    clearMessages()
    publishState()
}

internal fun AppStoreCore.onRefreshSelectedRepository() {
    val repositoryPath = selectedRepository()?.path ?: return
    clearMessages()
    scope.launch { refreshInstalledEditors() }
    loadWorktreesForRepository(path = repositoryPath)
    onLoadBranches()
}

internal fun AppStoreCore.onCreateWorktreeBranchChanged(value: String) {
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

internal fun AppStoreCore.onCreateWorktreePathChanged(value: String) {
    createWorktreeState =
        createWorktreeState.copy(
            worktreePathInput = value,
            createdWorktreePath = null,
        )
    clearMessages()
    publishState()
}

internal fun AppStoreCore.onCreateWorktreeBaseBranchChanged(value: String) {
    createWorktreeState = createWorktreeState.copy(baseBranchInput = value)
    clearMessages()
    publishState()
}

internal fun AppStoreCore.onCreateWorktreeCreateBranchChanged(value: Boolean) {
    createWorktreeState = createWorktreeState.copy(createBranch = value)
    clearMessages()
    publishState()
}

internal fun AppStoreCore.onCreateWorktree() {
    if (createWorktreeState.isSubmitting || activityCenter.isGlobalActive) {
        return
    }
    val repositoryPath = selectedRepository()?.path ?: return
    scope.launch {
        val repository = selectedRepository()
        if (repository == null) {
            createWorktreeState = createWorktreeState.copy(isSubmitting = false)
            return@launch
        }
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
                val preferredBaseBranch = createWorktreeState.baseBranchInput.trim().ifBlank { null }
                preferredBaseBranch?.let { baseBranch ->
                    graph.preferencesStore.setPreferredBaseBranch(
                        branch = baseBranch,
                        forRepositoryId = repository.id,
                    )
                    graph.preferencesStore.setWorktreeBaseBranch(
                        branch = baseBranch,
                        forWorktreePath = result.value.createdWorktree.path,
                    )
                }
                copyConfiguredFiles(
                    repositoryPath = repository.path,
                    createdWorktreePath = result.value.createdWorktree.path,
                    repositoryId = repository.id.value,
                )
                val worktrees = graph.gitClient.listWorktrees(atRepoPath = repositoryPath)
                worktreesByRepositoryPath[repositoryPath] = worktrees
                selectedWorktreePath = result.value.createdWorktree.path
                persistSelection()
                createWorktreeState =
                    createWorktreeState.copy(
                        isSubmitting = false,
                        createdWorktreePath = result.value.createdWorktree.path,
                    )
                onLoadBranches()
                success =
                    AppStore.SuccessState(
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

internal fun AppStoreCore.loadWorktreesForRepository(path: String) {
    scope.launch {
        withGlobalLoading(Res.string.loading_refreshing) {
            loadWorktreesForRepositoryInternal(path = path)
        }
    }
}

internal suspend fun AppStoreCore.loadWorktreesForRepositoryInternal(path: String) {
    val normalizedPath = normalizePath(path)
    if (normalizedPath.isBlank()) {
        return
    }
    runCatching {
        graph.gitClient.listWorktrees(atRepoPath = normalizedPath)
    }.onSuccess { worktrees ->
        worktreesByRepositoryPath[normalizedPath] =
            worktrees.map { worktree ->
                val baseBranch =
                    graph.preferencesStore.worktreeBaseBranch(
                        forWorktreePath = worktree.path,
                    )
                worktree.withBaseBranch(baseBranch = baseBranch)
            }
        if (selectedRepository()?.path == normalizedPath && selectedWorktreePath != null) {
            val stillExists = worktreesByRepositoryPath[normalizedPath].orEmpty().any { it.path == selectedWorktreePath }
            if (!stillExists) {
                selectedWorktreePath = null
                persistSelection()
            }
        }
        worktreesByRepositoryPath[normalizedPath].orEmpty().forEach { worktree ->
            if (!worktreeStatusByPath.containsKey(worktree.path)) {
                onRefreshWorktreeStatus(worktreePath = worktree.path)
            }
        }
    }.onFailure { throwable ->
        val domainFailure = DomainFailureMapper.fromThrowable(throwable)
        error = mapFailureToErrorState(domainFailure)
    }
}
