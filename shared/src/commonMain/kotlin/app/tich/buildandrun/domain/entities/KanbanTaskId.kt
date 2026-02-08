package app.tich.buildandrun.domain.entities

value class KanbanTaskId(val value: String) {
    init {
        require(value.isNotBlank()) { "KanbanTaskId cannot be blank" }
    }

    companion object {
        fun generate(): KanbanTaskId = KanbanTaskId(generateUuid())
    }
}
