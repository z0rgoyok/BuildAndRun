package app.tich.buildandrun.presentation.app.context.kanban.impl

import app.tich.buildandrun.domain.context.kanban.model.KanbanColumnType
import app.tich.buildandrun.domain.context.kanban.model.KanbanTask
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode
import app.tich.buildandrun.presentation.app.AppKanbanFeature
import app.tich.buildandrun.presentation.app.ErrorState
import app.tich.buildandrun.presentation.app.context.state.KanbanContextState
import app.tich.buildandrun.presentation.app.context.state.MessagesContextState
import app.tich.buildandrun.presentation.app.context.state.repositoryScopeKey
import app.tich.buildandrun.presentation.app.core.AppStateRefresher
import app.tich.buildandrun.presentation.app.core.currentEpochMillis
import app.tich.buildandrun.presentation.app.core.resolveText
import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.resources.Res
import app.tich.buildandrun.resources.app_validation_task_title_blank

class AppKanbanService(
    private val stateRefresher: AppStateRefresher,
    private val kanbanState: KanbanContextState,
    private val messagesState: MessagesContextState,
) : AppKanbanFeature {
    override fun onAddTask(
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
        persistKanbanTasks(scope = scope, tasks = updatedTasks)
        messagesState.clear()
        stateRefresher.publishAll()
    }

    override fun onMoveTask(
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
        persistKanbanTasks(scope = scope, tasks = updatedTasks)
        messagesState.clear()
        stateRefresher.publishAll()
    }

    override fun onDeleteTask(taskId: String) {
        val scope = currentKanbanScope() ?: return
        val updatedTasks = scope.existingTasks.filterNot { it.id.value == taskId }
        if (updatedTasks.size == scope.existingTasks.size) {
            return
        }
        persistKanbanTasks(scope = scope, tasks = updatedTasks)
        messagesState.clear()
        stateRefresher.publishAll()
    }

    override fun onUpdateTask(
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
        persistKanbanTasks(scope = scope, tasks = updatedTasks)
        messagesState.clear()
        stateRefresher.publishAll()
    }

    private fun currentKanbanScope(): KanbanScope? {
        val repositoryId = stateRefresher.currentRepositoryId() ?: return null
        val scopeKey = repositoryScopeKey(repositoryId = repositoryId)
        return KanbanScope(
            repositoryId = repositoryId,
            scopeKey = scopeKey,
            existingTasks = kanbanState.tasksByScope[scopeKey].orEmpty(),
        )
    }

    private fun persistKanbanTasks(
        scope: KanbanScope,
        tasks: List<KanbanTask>,
    ) {
        kanbanState.tasksByScope[scope.scopeKey] = tasks.toMutableList()
        stateRefresher.persistKanbanTasksForRepository(
            repositoryId = scope.repositoryId,
            tasks = tasks,
        )
    }

    private fun normalizedTaskTitleOrPublishError(title: String): String? {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isBlank()) {
            messagesState.error =
                ErrorState(
                    code = DomainFailureCode.APP_VALIDATION_TASK_TITLE_BLANK,
                    message = resolveText(text = UiText(resource = Res.string.app_validation_task_title_blank)),
                    details = null,
                    isRetryable = false,
                )
            stateRefresher.publishAll()
            return null
        }
        return normalizedTitle
    }

    private class KanbanScope(
        val repositoryId: String,
        val scopeKey: String,
        val existingTasks: List<KanbanTask>,
    )
}
