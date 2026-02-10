package app.tich.buildandrun.presentation.app.context.worktrees.impl

import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper
import app.tich.buildandrun.presentation.app.core.AppRuntime
import app.tich.buildandrun.presentation.app.core.mapFailureToErrorState
import app.tich.buildandrun.presentation.app.core.normalizePath
import app.tich.buildandrun.presentation.app.core.publishState
import kotlinx.coroutines.launch

internal fun AppRuntime.onRefreshWorktreeStatus(worktreePath: String) {
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
