package app.tich.buildandrun.appstore

import app.tich.buildandrun.domain.usecases.LoadBranchesUseCase
import app.tich.buildandrun.domain.usecases.UseCaseResult
import kotlinx.coroutines.launch

internal fun AppStoreCore.onLoadBranches() {
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
                branches = result.value.branches
            }

            is UseCaseResult.Failure -> {
                error = mapFailureToErrorState(result.value)
            }
        }
        publishState()
    }
}

internal fun AppStoreCore.branchExists(branch: String): Boolean {
    selectedRepository()?.path ?: return false
    val normalizedBranch = branch.trim()
    if (normalizedBranch.isBlank()) {
        return false
    }
    return branches.any { it == normalizedBranch }
}

internal fun AppStoreCore.onSetWorktreeBasePath(path: String) {
    val normalizedPath = path.trim()
    worktreeBasePath = normalizedPath
    graph.preferencesStore.worktreeBasePath = normalizedPath
    publishState()
}

internal fun AppStoreCore.preferredBaseBranch(): String? {
    val repository = selectedRepository() ?: return null
    return graph.preferencesStore.preferredBaseBranch(forRepositoryId = repository.id)
}

internal fun AppStoreCore.onSetPreferredBaseBranch(branch: String) {
    val repository = selectedRepository() ?: return
    val normalizedBranch = branch.trim()
    if (normalizedBranch.isBlank()) {
        return
    }
    graph.preferencesStore.setPreferredBaseBranch(
        branch = normalizedBranch,
        forRepositoryId = repository.id,
    )
    createWorktreeState = createWorktreeState.copy(baseBranchInput = normalizedBranch)
    publishState()
}

internal fun AppStoreCore.onSetDefaultCopyPatterns(patterns: List<String>) {
    val normalizedPatterns =
        patterns
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .map { pattern -> app.tich.buildandrun.domain.entities.CopyPattern(pattern = pattern) }
    defaultCopyPatterns = normalizedPatterns
    graph.preferencesStore.defaultCopyPatterns = normalizedPatterns
    publishState()
}

internal fun AppStoreCore.onSetRepositoryCopyPatterns(patterns: List<String>?) {
    val repository = selectedRepository() ?: return
    if (patterns == null) {
        graph.preferencesStore.removeCopyPatterns(forRepositoryId = repository.id)
    } else {
        val normalizedPatterns =
            patterns
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct()
                .map { pattern -> app.tich.buildandrun.domain.entities.CopyPattern(pattern = pattern) }
        graph.preferencesStore.setCopyPatterns(
            patterns = normalizedPatterns,
            forRepositoryId = repository.id,
        )
    }
    publishState()
}
