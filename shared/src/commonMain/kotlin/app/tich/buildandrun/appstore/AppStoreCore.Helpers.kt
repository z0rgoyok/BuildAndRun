package app.tich.buildandrun.appstore

import app.tich.buildandrun.domain.entities.KanbanColumnType
import app.tich.buildandrun.domain.entities.KanbanTask
import app.tich.buildandrun.domain.entities.Repository
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
    tasksByScope.remove("repo:${repository.id.value}")
    removedWorktreePaths.forEach { tasksByScope.remove("worktree:$it") }
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

internal fun createDefaultTasks(worktreePath: String?): MutableList<KanbanTask> {
    val now = currentEpochMillis()
    return mutableListOf(
        KanbanTask.create(
            title = "Add unit tests",
            description = "Cover UserService with tests",
            columnId = KanbanColumnType.TODO,
            worktreePath = worktreePath,
            createdAt = now,
            order = 1,
        ),
        KanbanTask.create(
            title = "Update README",
            description = "Add setup instructions for new developers",
            columnId = KanbanColumnType.TODO,
            worktreePath = worktreePath,
            createdAt = now,
            order = 2,
        ),
        KanbanTask.create(
            title = "Refactor data layer",
            description = null,
            columnId = KanbanColumnType.TODO,
            worktreePath = worktreePath,
            createdAt = now,
            order = 3,
        ),
        KanbanTask.create(
            title = "Fix navigation bug",
            description = "Back button not working on detail screen",
            columnId = KanbanColumnType.IN_PROGRESS,
            worktreePath = worktreePath,
            createdAt = now,
            order = 1,
        ),
        KanbanTask.create(
            title = "Review PR #42",
            description = null,
            columnId = KanbanColumnType.REVIEW,
            worktreePath = worktreePath,
            createdAt = now,
            order = 1,
        ),
        KanbanTask.create(
            title = "Implement authentication flow",
            description = "Add login/logout functionality with OAuth2",
            columnId = KanbanColumnType.DONE,
            worktreePath = worktreePath,
            createdAt = now,
            order = 1,
        ),
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
