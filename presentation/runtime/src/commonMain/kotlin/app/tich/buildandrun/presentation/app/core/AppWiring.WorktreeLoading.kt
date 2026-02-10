package app.tich.buildandrun.presentation.app.core

import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper
import app.tich.buildandrun.resources.Res
import app.tich.buildandrun.resources.loading_refreshing
import kotlinx.coroutines.launch

fun AppWiring.loadWorktreesForRepository(path: String) {
    scope.launch {
        withGlobalLoading(Res.string.loading_refreshing) {
            loadWorktreesForRepositoryInternal(path = path)
        }
    }
}

suspend fun AppWiring.loadWorktreesForRepositoryInternal(path: String) {
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
            val stillExists =
                worktreesState.worktreesByRepositoryPath[normalizedPath]
                    .orEmpty()
                    .any { it.path == worktreesState.selectedWorktreePath }
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
