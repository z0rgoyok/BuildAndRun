package app.tich.buildandrun.presentation.app

import app.tich.buildandrun.domain.context.kanban.model.KanbanColumnType

interface AppKanbanFeature {
    fun onAddTask(
        title: String,
        description: String?,
        column: KanbanColumnType,
    )

    fun onMoveTask(
        taskId: String,
        column: KanbanColumnType,
    )

    fun onDeleteTask(taskId: String)

    fun onUpdateTask(
        taskId: String,
        title: String,
        description: String?,
    )
}
