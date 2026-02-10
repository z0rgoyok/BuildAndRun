package app.tich.buildandrun.appstore

import app.tich.buildandrun.domain.entities.KanbanTask
import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.entities.RepositoryId
import app.tich.buildandrun.domain.entities.Worktree
import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.presentation.i18n.UiTextLocalizer
import org.jetbrains.compose.resources.StringResource

internal fun suggestWorktreePath(
    repositoryPath: String,
    branch: String,
): String {
    val repoName = repositoryPath.substringAfterLast('/')
    val parentPath = repositoryPath.substringBeforeLast('/', missingDelimiterValue = "")
    val normalizedBranch =
        branch
            .trim()
            .replace("/", "-")
            .replace("\\", "-")
            .replace(" ", "-")
    return if (parentPath.isBlank()) {
        "$repositoryPath-$normalizedBranch"
    } else {
        "$parentPath/$repoName-$normalizedBranch"
    }
}

internal fun normalizePath(path: String): String = path.trim().trimEnd('/')

internal fun currentEpochMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()

internal fun AppStoreCore.cleanupRepositoryData(repository: Repository) {
    val removedWorktreePaths = worktreesByRepositoryPath.remove(repository.path).orEmpty().map { it.path }
    tasksByScope.remove(repositoryScopeKey(repositoryId = repository.id.value))
    graph.preferencesStore.removeKanbanTasks(forRepositoryId = repository.id)
    removedWorktreePaths.forEach { worktreeStatusByPath.remove(it) }
    removedWorktreePaths.forEach { hasRemoteBranchByWorktreePath.remove(it) }
    worktreeStatusLoadingPaths.removeAll(removedWorktreePaths.toSet())
}

internal fun AppStoreCore.findWorktreeByPath(path: String): Pair<Repository, Worktree>? {
    val normalizedPath = normalizePath(path)
    if (normalizedPath.isBlank()) {
        return null
    }
    repositories.forEach { repository ->
        val worktree = worktreesByRepositoryPath[repository.path].orEmpty().firstOrNull { normalizePath(it.path) == normalizedPath }
        if (worktree != null) {
            return repository to worktree
        }
    }
    return null
}

internal fun AppStoreCore.currentRepositoryId(): String? = selectedRepository()?.id?.value

internal fun AppStoreCore.persistSelection() {
    graph.preferencesStore.lastSelectedRepositoryId = selectedRepositoryId
    graph.preferencesStore.lastSelectedWorktreePath = selectedWorktreePath
}

internal fun repositoryScopeKey(repositoryId: String): String = "repo:$repositoryId"

internal fun AppStoreCore.persistKanbanTasksForRepository(
    repositoryId: String,
    tasks: List<KanbanTask>,
) {
    graph.preferencesStore.setKanbanTasks(
        tasks = tasks,
        forRepositoryId = RepositoryId(value = repositoryId),
    )
}

internal fun resolveText(text: UiText): String = UiTextLocalizer.resolve(text = text)

internal suspend fun <T> AppStoreCore.withGlobalLoading(
    resource: StringResource,
    block: suspend () -> T,
): T {
    val tokenId = activityCenter.beginGlobal(resolveText(UiText(resource)))
    clearMessages()
    publishState()
    try {
        return block()
    } finally {
        activityCenter.end(tokenId)
        publishState()
    }
}

internal suspend fun <T> AppStoreCore.withWorktreeLoading(
    worktreePath: String,
    resource: StringResource,
    block: suspend () -> T,
): T {
    val tokenId = activityCenter.beginWorktree(worktreePath, resolveText(UiText(resource)))
    clearMessages()
    publishState()
    try {
        return block()
    } finally {
        activityCenter.end(tokenId)
        publishState()
    }
}
