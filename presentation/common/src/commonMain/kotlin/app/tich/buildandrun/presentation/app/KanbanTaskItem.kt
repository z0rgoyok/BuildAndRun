package app.tich.buildandrun.presentation.app

import app.tich.buildandrun.domain.context.kanban.model.KanbanColumnType

data class KanbanTaskItem(
    val id: String,
    val title: String,
    val description: String?,
    val columnId: KanbanColumnType,
    val order: Int,
)
