package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.entities.KanbanColumnType
import app.tich.buildandrun.domain.entities.KanbanTask
import app.tich.buildandrun.domain.failures.DomainFailureCode
import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.resources.Res
import app.tich.buildandrun.resources.app_validation_task_title_blank

internal fun MacOSAppStoreCore.onAddTask(
    title: String,
    description: String?,
    column: KanbanColumnType,
) {
    val normalizedTitle = title.trim()
    if (normalizedTitle.isBlank()) {
        error =
            MacOSAppStore.ErrorState(
                code = DomainFailureCode.APP_VALIDATION_TASK_TITLE_BLANK,
                message = resolveText(text = UiText(resource = Res.string.app_validation_task_title_blank)),
                details = null,
                isRetryable = false,
            )
        publishState()
        return
    }
    val scopeKey = selectedScopeKey() ?: return
    val existingTasks = tasksByScope.getOrPut(scopeKey) { createDefaultTasks(currentWorktreePath()) }
    val maxOrder = existingTasks.filter { it.columnId == column }.maxOfOrNull { it.order } ?: 0
    existingTasks +=
        KanbanTask.create(
            title = normalizedTitle,
            description = description?.trim()?.ifBlank { null },
            columnId = column,
            worktreePath = currentWorktreePath(),
            createdAt = currentEpochMillis(),
            order = maxOrder + 1,
        )
    clearMessages()
    publishState()
}

internal fun MacOSAppStoreCore.onMoveTask(
    taskId: String,
    column: KanbanColumnType,
) {
    val scopeKey = selectedScopeKey() ?: return
    val existingTasks = tasksByScope.getOrPut(scopeKey) { createDefaultTasks(currentWorktreePath()) }
    val index = existingTasks.indexOfFirst { it.id.value == taskId }
    if (index == -1) {
        return
    }
    val maxOrder = existingTasks.filter { it.columnId == column }.maxOfOrNull { it.order } ?: 0
    existingTasks[index] = existingTasks[index].moveTo(column).withOrder(maxOrder + 1)
    clearMessages()
    publishState()
}

internal fun MacOSAppStoreCore.onDeleteTask(taskId: String) {
    val scopeKey = selectedScopeKey() ?: return
    val existingTasks = tasksByScope.getOrPut(scopeKey) { createDefaultTasks(currentWorktreePath()) }
    if (existingTasks.removeAll { it.id.value == taskId }) {
        clearMessages()
        publishState()
    }
}
