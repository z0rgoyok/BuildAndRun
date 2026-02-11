package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.kanban.model.KanbanColumnType
import app.tich.buildandrun.domain.context.kanban.model.KanbanTask
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

class AddKanbanTaskUseCase {
    fun execute(input: Input): UseCaseResult<Output> {
        val title = input.title.trim()
        if (title.isBlank()) {
            return UseCaseResult.Failure(
                value =
                    DomainFailure.Validation(
                        code = DomainFailureCode.APP_VALIDATION_TASK_TITLE_BLANK,
                        args = emptyList(),
                    ),
            )
        }

        val maxOrder = input.currentTasks.filter { it.columnId == input.column }.maxOfOrNull { it.order } ?: 0
        val tasks = input.currentTasks.toMutableList()
        tasks +=
            KanbanTask.create(
                title = title,
                description = input.description?.trim()?.ifBlank { null },
                columnId = input.column,
                worktreePath = null,
                createdAt = input.createdAt,
                order = maxOrder + 1,
            )
        return UseCaseResult.Success(value = Output(tasks = tasks))
    }

    data class Input(
        val title: String,
        val description: String?,
        val column: KanbanColumnType,
        val createdAt: Long,
        val currentTasks: List<KanbanTask>,
    )

    data class Output(
        val tasks: List<KanbanTask>,
    )
}
