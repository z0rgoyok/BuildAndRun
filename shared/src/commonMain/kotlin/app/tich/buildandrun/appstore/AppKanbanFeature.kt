package app.tich.buildandrun.appstore

import app.tich.buildandrun.domain.entities.KanbanColumnType

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
