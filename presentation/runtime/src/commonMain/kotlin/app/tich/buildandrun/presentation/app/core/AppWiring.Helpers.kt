package app.tich.buildandrun.presentation.app.core

import app.tich.buildandrun.domain.context.kanban.model.KanbanTask
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId
import app.tich.buildandrun.domain.context.worktrees.model.Worktree
import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.presentation.i18n.UiTextLocalizer
import org.jetbrains.compose.resources.StringResource

fun suggestWorktreePath(
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

fun normalizePath(path: String): String = path.trim().trimEnd('/')

fun currentEpochMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()

fun AppWiring.cleanupRepositoryData(repository: Repository) {
    val removedWorktreePaths = worktreesState.worktreesByRepositoryPath.remove(repository.path).orEmpty().map { it.path }
    kanbanState.tasksByScope.remove(repositoryScopeKey(repositoryId = repository.id.value))
    graph.preferencesStore.removeKanbanTasks(forRepositoryId = repository.id)
    removedWorktreePaths.forEach { worktreesState.worktreeStatusByPath.remove(it) }
    removedWorktreePaths.forEach { worktreesState.hasRemoteBranchByWorktreePath.remove(it) }
    worktreesState.worktreeStatusLoadingPaths.removeAll(removedWorktreePaths.toSet())
}

fun AppWiring.findWorktreeByPath(path: String): Pair<Repository, Worktree>? {
    val normalizedPath = normalizePath(path)
    if (normalizedPath.isBlank()) {
        return null
    }
    repositoriesState.repositories.forEach { repository ->
        val worktree =
            worktreesState.worktreesByRepositoryPath[repository.path]
                .orEmpty()
                .firstOrNull { normalizePath(it.path) == normalizedPath }
        if (worktree != null) {
            return repository to worktree
        }
    }
    return null
}

fun AppWiring.currentRepositoryId(): String? = selectedRepository()?.id?.value

fun AppWiring.persistSelection() {
    graph.preferencesStore.lastSelectedRepositoryId = repositoriesState.selectedRepositoryId
    graph.preferencesStore.lastSelectedWorktreePath = worktreesState.selectedWorktreePath
}

fun repositoryScopeKey(repositoryId: String): String = "repo:$repositoryId"

fun AppWiring.persistKanbanTasksForRepository(
    repositoryId: String,
    tasks: List<KanbanTask>,
) {
    graph.preferencesStore.setKanbanTasks(
        tasks = tasks,
        forRepositoryId = RepositoryId(value = repositoryId),
    )
}

fun resolveText(text: UiText): String = UiTextLocalizer.resolve(text = text)

suspend fun <T> AppWiring.withGlobalLoading(
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

suspend fun <T> AppWiring.withWorktreeLoading(
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
