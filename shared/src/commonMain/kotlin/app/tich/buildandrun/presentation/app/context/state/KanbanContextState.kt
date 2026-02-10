package app.tich.buildandrun.presentation.app.context.state

import app.tich.buildandrun.domain.context.kanban.model.KanbanTask

internal class KanbanContextState {
    val tasksByScope = mutableMapOf<String, MutableList<KanbanTask>>()
}
