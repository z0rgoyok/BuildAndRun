package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.entities.KanbanColumnType
import app.tich.buildandrun.domain.entities.KanbanTask
import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.presentation.i18n.UiTextLocalizer

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

internal fun MacOSAppStoreCore.cleanupRepositoryData(repository: Repository) {
    val removedWorktreePaths = worktreesByRepositoryPath.remove(repository.path).orEmpty().map { it.path }
    tasksByScope.remove("repo:${repository.id.value}")
    removedWorktreePaths.forEach { tasksByScope.remove("worktree:$it") }
    removedWorktreePaths.forEach { worktreeStatusByPath.remove(it) }
    worktreeStatusLoadingPaths.removeAll(removedWorktreePaths.toSet())
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
