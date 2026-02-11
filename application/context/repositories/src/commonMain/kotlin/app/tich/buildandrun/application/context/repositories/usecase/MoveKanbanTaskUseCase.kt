package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.kanban.model.KanbanColumnType
import app.tich.buildandrun.domain.context.kanban.model.KanbanTask

class MoveKanbanTaskUseCase {
    fun execute(input: Input): UseCaseResult<Output> {
        val index = input.currentTasks.indexOfFirst { it.id.value == input.taskId }
        if (index == -1) {
            return UseCaseResult.Success(value = Output(tasks = input.currentTasks, changed = false))
        }
        val maxOrder = input.currentTasks.filter { it.columnId == input.column }.maxOfOrNull { it.order } ?: 0
        val tasks = input.currentTasks.toMutableList()
        tasks[index] = tasks[index].moveTo(input.column).withOrder(maxOrder + 1)
        return UseCaseResult.Success(value = Output(tasks = tasks, changed = true))
    }

    data class Input(
        val taskId: String,
        val column: KanbanColumnType,
        val currentTasks: List<KanbanTask>,
    )

    data class Output(
        val tasks: List<KanbanTask>,
        val changed: Boolean,
    )
}
