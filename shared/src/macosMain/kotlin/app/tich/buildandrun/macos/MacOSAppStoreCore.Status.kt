package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.failures.DomainFailureMapper
import kotlinx.coroutines.launch

internal fun MacOSAppStoreCore.onRefreshWorktreeStatus(worktreePath: String) {
    val normalizedPath = normalizePath(worktreePath)
    if (normalizedPath.isBlank()) {
        return
    }
    if (worktreeStatusLoadingPaths.contains(normalizedPath)) {
        return
    }
    scope.launch {
        worktreeStatusLoadingPaths += normalizedPath
        publishState()
        runCatching {
            graph.gitClient.getWorktreeStatus(atWorktreePath = normalizedPath)
        }.onSuccess { status ->
            worktreeStatusByPath[normalizedPath] = status
        }.onFailure { throwable ->
            val domainFailure = DomainFailureMapper.fromThrowable(throwable)
            error = mapFailureToErrorState(domainFailure)
        }
        worktreeStatusLoadingPaths -= normalizedPath
        publishState()
    }
}
