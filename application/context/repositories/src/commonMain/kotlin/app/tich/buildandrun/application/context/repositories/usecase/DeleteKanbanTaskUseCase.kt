package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.kanban.model.KanbanTask

class DeleteKanbanTaskUseCase {
    fun execute(input: Input): UseCaseResult<Output> {
        val tasks = input.currentTasks.filterNot { it.id.value == input.taskId }
        return UseCaseResult.Success(
            value =
                Output(
                    tasks = tasks,
                    changed = tasks.size != input.currentTasks.size,
                ),
        )
    }

    data class Input(
        val taskId: String,
        val currentTasks: List<KanbanTask>,
    )

    data class Output(
        val tasks: List<KanbanTask>,
        val changed: Boolean,
    )
}
