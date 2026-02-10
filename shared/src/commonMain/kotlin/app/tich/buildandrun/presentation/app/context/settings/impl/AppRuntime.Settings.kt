package app.tich.buildandrun.presentation.app.context.settings.impl

import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.worktrees.usecase.LoadBranchesUseCase
import app.tich.buildandrun.presentation.app.core.AppRuntime
import app.tich.buildandrun.presentation.app.core.mapFailureToErrorState
import app.tich.buildandrun.presentation.app.core.publishState
import app.tich.buildandrun.presentation.app.core.selectedRepository
import kotlinx.coroutines.launch

internal fun AppRuntime.onLoadBranches() {
    val repositoryPath = selectedRepository()?.path ?: return
    scope.launch {
        when (
            val result =
                graph.loadBranchesUseCase.execute(
                    input =
                        LoadBranchesUseCase.Input(
                            repositoryPath = repositoryPath,
                        ),
                )
        ) {
            is UseCaseResult.Success -> {
                settingsState.branches = result.value.branches
            }

            is UseCaseResult.Failure -> {
                messagesState.error = mapFailureToErrorState(result.value)
            }
        }
        publishState()
    }
}

internal fun AppRuntime.branchExists(branch: String): Boolean {
    selectedRepository()?.path ?: return false
    val normalizedBranch = branch.trim()
    if (normalizedBranch.isBlank()) {
        return false
    }
    return settingsState.branches.any { it == normalizedBranch }
}

internal fun AppRuntime.onSetWorktreeBasePath(path: String) {
    val normalizedPath = path.trim()
    settingsState.worktreeBasePath = normalizedPath
    graph.preferencesStore.worktreeBasePath = normalizedPath
    publishState()
}

internal fun AppRuntime.preferredBaseBranch(): String? {
    val repository = selectedRepository() ?: return null
    return graph.preferencesStore.preferredBaseBranch(forRepositoryId = repository.id)
}

internal fun AppRuntime.onSetPreferredBaseBranch(branch: String) {
    val repository = selectedRepository() ?: return
    val normalizedBranch = branch.trim()
    if (normalizedBranch.isBlank()) {
        return
    }
    graph.preferencesStore.setPreferredBaseBranch(
        branch = normalizedBranch,
        forRepositoryId = repository.id,
    )
    worktreesState.createWorktreeState = worktreesState.createWorktreeState.copy(baseBranchInput = normalizedBranch)
    publishState()
}

internal fun AppRuntime.onSetDefaultCopyPatterns(patterns: List<String>) {
    val normalizedPatterns =
        patterns
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .map { pattern -> app.tich.buildandrun.domain.context.copy.model.CopyPattern(pattern = pattern) }
    settingsState.defaultCopyPatterns = normalizedPatterns
    graph.preferencesStore.defaultCopyPatterns = normalizedPatterns
    publishState()
}

internal fun AppRuntime.onSetRepositoryCopyPatterns(patterns: List<String>?) {
    val repository = selectedRepository() ?: return
    if (patterns == null) {
        graph.preferencesStore.removeCopyPatterns(forRepositoryId = repository.id)
    } else {
        val normalizedPatterns =
            patterns
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct()
                .map { pattern -> app.tich.buildandrun.domain.context.copy.model.CopyPattern(pattern = pattern) }
        graph.preferencesStore.setCopyPatterns(
            patterns = normalizedPatterns,
            forRepositoryId = repository.id,
        )
    }
    publishState()
}
