package app.tich.buildandrun.appstore

import app.tich.buildandrun.domain.entities.KanbanColumnType

internal class AppKanbanService(
    private val runtime: AppRuntime,
) : AppKanbanFeature {
    override fun onAddTask(
        title: String,
        description: String?,
        column: KanbanColumnType,
    ) {
        runtime.onAddTask(
            title = title,
            description = description,
            column = column,
        )
    }

    override fun onMoveTask(
        taskId: String,
        column: KanbanColumnType,
    ) {
        runtime.onMoveTask(
            taskId = taskId,
            column = column,
        )
    }

    override fun onDeleteTask(taskId: String) {
        runtime.onDeleteTask(taskId = taskId)
    }

    override fun onUpdateTask(
        taskId: String,
        title: String,
        description: String?,
    ) {
        runtime.onUpdateTask(
            taskId = taskId,
            title = title,
            description = description,
        )
    }
}
