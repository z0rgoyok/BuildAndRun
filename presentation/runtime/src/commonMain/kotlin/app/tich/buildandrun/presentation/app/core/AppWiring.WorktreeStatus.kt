package app.tich.buildandrun.presentation.app.core

import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper
import kotlinx.coroutines.launch

fun AppWiring.onRefreshWorktreeStatus(worktreePath: String) {
    val normalizedPath = normalizePath(worktreePath)
    if (normalizedPath.isBlank()) {
        return
    }
    if (worktreesState.worktreeStatusLoadingPaths.contains(normalizedPath)) {
        return
    }
    scope.launch {
        worktreesState.worktreeStatusLoadingPaths += normalizedPath
        publishState()
        runCatching {
            graph.gitClient.getWorktreeStatus(atWorktreePath = normalizedPath)
        }.onSuccess { status ->
            worktreesState.worktreeStatusByPath[normalizedPath] = status
        }.onFailure { throwable ->
            val domainFailure = DomainFailureMapper.fromThrowable(throwable)
            messagesState.error = mapFailureToErrorState(domainFailure)
        }
        worktreesState.worktreeStatusLoadingPaths -= normalizedPath
        publishState()
    }
}
