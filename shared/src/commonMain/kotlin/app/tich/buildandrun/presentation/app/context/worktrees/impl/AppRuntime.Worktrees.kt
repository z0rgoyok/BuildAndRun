package app.tich.buildandrun.presentation.app.context.worktrees.impl

import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.worktrees.usecase.CreateWorktreeUseCase
import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper
import app.tich.buildandrun.presentation.app.SuccessState
import app.tich.buildandrun.presentation.app.context.settings.impl.onLoadBranches
import app.tich.buildandrun.presentation.app.core.*
import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.resources.Res
import app.tich.buildandrun.resources.loading_refreshing
import app.tich.buildandrun.resources.screen_create_worktree_success
import kotlinx.coroutines.launch

internal fun AppRuntime.onSelectWorktree(worktreePath: String?) {
    worktreesState.selectedWorktreePath = worktreePath
    persistSelection()
    clearMessages()
    publishState()
}

internal fun AppRuntime.onRefreshSelectedRepository() {
    val repositoryPath = selectedRepository()?.path ?: return
    clearMessages()
    scope.launch { refreshInstalledEditors() }
    loadWorktreesForRepository(path = repositoryPath)
    onLoadBranches()
}

internal fun AppRuntime.onCreateWorktreeBranchChanged(value: String) {
    val selectedRepositoryPath = selectedRepository()?.path.orEmpty()
    val normalizedBranch = value.trim()
    val currentWorktreePath = worktreesState.createWorktreeState.worktreePathInput
    val updatedWorktreePath =
        if (currentWorktreePath.isBlank() && normalizedBranch.isNotBlank() && selectedRepositoryPath.isNotBlank()) {
            suggestWorktreePath(repositoryPath = selectedRepositoryPath, branch = normalizedBranch)
        } else {
            currentWorktreePath
        }
    worktreesState.createWorktreeState =
        worktreesState.createWorktreeState.copy(
            branchInput = value,
            worktreePathInput = updatedWorktreePath,
            createdWorktreePath = null,
        )
    clearMessages()
    publishState()
}

internal fun AppRuntime.onCreateWorktreePathChanged(value: String) {
    worktreesState.createWorktreeState =
        worktreesState.createWorktreeState.copy(
            worktreePathInput = value,
            createdWorktreePath = null,
        )
    clearMessages()
    publishState()
}

internal fun AppRuntime.onCreateWorktreeBaseBranchChanged(value: String) {
    worktreesState.createWorktreeState = worktreesState.createWorktreeState.copy(baseBranchInput = value)
    clearMessages()
    publishState()
}

internal fun AppRuntime.onCreateWorktreeCreateBranchChanged(value: Boolean) {
    worktreesState.createWorktreeState = worktreesState.createWorktreeState.copy(createBranch = value)
    clearMessages()
    publishState()
}

internal fun AppRuntime.onCreateWorktree() {
    if (worktreesState.createWorktreeState.isSubmitting || activityCenter.isGlobalActive) {
        return
    }
    val repositoryPath = selectedRepository()?.path ?: return
    scope.launch {
        val repository = selectedRepository()
        if (repository == null) {
            worktreesState.createWorktreeState = worktreesState.createWorktreeState.copy(isSubmitting = false)
            return@launch
        }
        worktreesState.createWorktreeState =
            worktreesState.createWorktreeState.copy(
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
                            branch = worktreesState.createWorktreeState.branchInput,
                            worktreePath = worktreesState.createWorktreeState.worktreePathInput,
                            createBranch = worktreesState.createWorktreeState.createBranch,
                            baseBranch = worktreesState.createWorktreeState.baseBranchInput,
                        ),
                )
        ) {
            is UseCaseResult.Success -> {
                val preferredBaseBranch = worktreesState.createWorktreeState.baseBranchInput.trim().ifBlank { null }
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
                worktreesState.worktreesByRepositoryPath[repositoryPath] = worktrees
                worktreesState.selectedWorktreePath = result.value.createdWorktree.path
                persistSelection()
                worktreesState.createWorktreeState =
                    worktreesState.createWorktreeState.copy(
                        isSubmitting = false,
                        createdWorktreePath = result.value.createdWorktree.path,
                    )
                onLoadBranches()
                messagesState.success =
                    SuccessState(
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
                worktreesState.createWorktreeState = worktreesState.createWorktreeState.copy(isSubmitting = false)
                messagesState.error = mapFailureToErrorState(result.value)
            }
        }
        publishState()
    }
}

internal fun AppRuntime.loadWorktreesForRepository(path: String) {
    scope.launch {
        withGlobalLoading(Res.string.loading_refreshing) {
            loadWorktreesForRepositoryInternal(path = path)
        }
    }
}

internal suspend fun AppRuntime.loadWorktreesForRepositoryInternal(path: String) {
    val normalizedPath = normalizePath(path)
    if (normalizedPath.isBlank()) {
        return
    }
    runCatching {
        graph.gitClient.listWorktrees(atRepoPath = normalizedPath)
    }.onSuccess { worktrees ->
        worktreesState.worktreesByRepositoryPath[normalizedPath] =
            worktrees.map { worktree ->
                val baseBranch =
                    graph.preferencesStore.worktreeBaseBranch(
                        forWorktreePath = worktree.path,
                    )
                worktree.withBaseBranch(baseBranch = baseBranch)
            }
        if (selectedRepository()?.path == normalizedPath && worktreesState.selectedWorktreePath != null) {
            val stillExists = worktreesState.worktreesByRepositoryPath[normalizedPath].orEmpty().any { it.path == worktreesState.selectedWorktreePath }
            if (!stillExists) {
                worktreesState.selectedWorktreePath = null
                persistSelection()
            }
        }
        worktreesState.worktreesByRepositoryPath[normalizedPath].orEmpty().forEach { worktree ->
            if (!worktreesState.worktreeStatusByPath.containsKey(worktree.path)) {
                onRefreshWorktreeStatus(worktreePath = worktree.path)
            }
        }
    }.onFailure { throwable ->
        val domainFailure = DomainFailureMapper.fromThrowable(throwable)
        messagesState.error = mapFailureToErrorState(domainFailure)
    }
}
