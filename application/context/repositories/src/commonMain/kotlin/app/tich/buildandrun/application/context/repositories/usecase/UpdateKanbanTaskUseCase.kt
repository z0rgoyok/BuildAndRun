package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.kanban.model.KanbanTask
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

class UpdateKanbanTaskUseCase {
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

        val index = input.currentTasks.indexOfFirst { it.id.value == input.taskId }
        if (index == -1) {
            return UseCaseResult.Success(value = Output(tasks = input.currentTasks, changed = false))
        }

        val tasks = input.currentTasks.toMutableList()
        tasks[index] =
            tasks[index].copy(
                title = title,
                description = input.description?.trim()?.ifBlank { null },
            )
        return UseCaseResult.Success(value = Output(tasks = tasks, changed = true))
    }

    data class Input(
        val taskId: String,
        val title: String,
        val description: String?,
        val currentTasks: List<KanbanTask>,
    )

    data class Output(
        val tasks: List<KanbanTask>,
        val changed: Boolean,
    )
}
