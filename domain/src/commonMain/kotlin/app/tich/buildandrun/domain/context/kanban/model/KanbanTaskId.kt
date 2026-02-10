package app.tich.buildandrun.domain.context.kanban.model

import app.tich.buildandrun.domain.shared.model.generateUuid

value class KanbanTaskId(val value: String) {
    init {
        require(value.isNotBlank()) { "KanbanTaskId cannot be blank" }
    }

    companion object {
        fun generate(): KanbanTaskId = KanbanTaskId(generateUuid())
    }
}
