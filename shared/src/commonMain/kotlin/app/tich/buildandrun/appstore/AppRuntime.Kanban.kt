package app.tich.buildandrun.appstore

import app.tich.buildandrun.domain.entities.KanbanColumnType
import app.tich.buildandrun.domain.entities.KanbanTask
import app.tich.buildandrun.domain.failures.DomainFailureCode
import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.resources.Res
import app.tich.buildandrun.resources.app_validation_task_title_blank

internal fun AppRuntime.onAddTask(
    title: String,
    description: String?,
    column: KanbanColumnType,
) {
    val normalizedTitle = normalizedTaskTitleOrPublishError(title = title) ?: return
    val scope = currentKanbanScope() ?: return
    val maxOrder = scope.existingTasks.filter { it.columnId == column }.maxOfOrNull { it.order } ?: 0
    val updatedTasks = scope.existingTasks.toMutableList()
    updatedTasks +=
        KanbanTask.create(
            title = normalizedTitle,
            description = description?.trim()?.ifBlank { null },
            columnId = column,
            worktreePath = null,
            createdAt = currentEpochMillis(),
            order = maxOrder + 1,
        )
    persistKanbanTasks(
        scope = scope,
        tasks = updatedTasks,
    )
    clearMessages()
    publishState()
}

internal fun AppRuntime.onMoveTask(
    taskId: String,
    column: KanbanColumnType,
) {
    val scope = currentKanbanScope() ?: return
    val index = scope.existingTasks.indexOfFirst { it.id.value == taskId }
    if (index == -1) {
        return
    }
    val maxOrder = scope.existingTasks.filter { it.columnId == column }.maxOfOrNull { it.order } ?: 0
    val updatedTasks = scope.existingTasks.toMutableList()
    updatedTasks[index] = updatedTasks[index].moveTo(column).withOrder(maxOrder + 1)
    persistKanbanTasks(
        scope = scope,
        tasks = updatedTasks,
    )
    clearMessages()
    publishState()
}

internal fun AppRuntime.onUpdateTask(
    taskId: String,
    title: String,
    description: String?,
) {
    val normalizedTitle = normalizedTaskTitleOrPublishError(title = title) ?: return
    val scope = currentKanbanScope() ?: return
    val index = scope.existingTasks.indexOfFirst { it.id.value == taskId }
    if (index == -1) {
        return
    }
    val updatedTasks = scope.existingTasks.toMutableList()
    updatedTasks[index] =
        updatedTasks[index].copy(
            title = normalizedTitle,
            description = description?.trim()?.ifBlank { null },
        )
    persistKanbanTasks(
        scope = scope,
        tasks = updatedTasks,
    )
    clearMessages()
    publishState()
}

internal fun AppRuntime.onDeleteTask(taskId: String) {
    val scope = currentKanbanScope() ?: return
    val updatedTasks = scope.existingTasks.filterNot { it.id.value == taskId }
    if (updatedTasks.size == scope.existingTasks.size) {
        return
    }
    persistKanbanTasks(
        scope = scope,
        tasks = updatedTasks,
    )
    clearMessages()
    publishState()
}

private fun AppRuntime.currentKanbanScope(): KanbanScope? {
    val repositoryId = currentRepositoryId() ?: return null
    val scopeKey = selectedScopeKey() ?: return null
    return KanbanScope(
        repositoryId = repositoryId,
        scopeKey = scopeKey,
        existingTasks = tasksByScope[scopeKey].orEmpty(),
    )
}

private fun AppRuntime.persistKanbanTasks(
    scope: KanbanScope,
    tasks: List<KanbanTask>,
) {
    tasksByScope[scope.scopeKey] = tasks.toMutableList()
    persistKanbanTasksForRepository(
        repositoryId = scope.repositoryId,
        tasks = tasks,
    )
}

private fun AppRuntime.normalizedTaskTitleOrPublishError(title: String): String? {
    val normalizedTitle = title.trim()
    if (normalizedTitle.isBlank()) {
        error =
            AppStore.ErrorState(
                code = DomainFailureCode.APP_VALIDATION_TASK_TITLE_BLANK,
                message = resolveText(text = UiText(resource = Res.string.app_validation_task_title_blank)),
                details = null,
                isRetryable = false,
            )
        publishState()
        return null
    }
    return normalizedTitle
}

private class KanbanScope(
    val repositoryId: String,
    val scopeKey: String,
    val existingTasks: List<KanbanTask>,
)
