package app.tich.buildandrun.domain.context.kanban.model

data class KanbanTask(
    val id: KanbanTaskId,
    val title: String,
    val description: String?,
    val columnId: KanbanColumnType,
    val worktreePath: String?,
    val createdAt: Long,
    val order: Int,
) {
    init {
        require(title.isNotBlank()) { "Task title cannot be blank" }
        require(order >= 0) { "Task order must be non-negative" }
    }

    val isProjectLevel: Boolean get() = worktreePath == null

    fun moveTo(column: KanbanColumnType): KanbanTask = copy(columnId = column)

    fun withOrder(newOrder: Int): KanbanTask = copy(order = newOrder)

    companion object {
        fun create(
            title: String,
            description: String? = null,
            columnId: KanbanColumnType = KanbanColumnType.TODO,
            worktreePath: String? = null,
            createdAt: Long,
            order: Int = 0,
        ): KanbanTask =
            KanbanTask(
                id = KanbanTaskId.generate(),
                title = title,
                description = description,
                columnId = columnId,
                worktreePath = worktreePath,
                createdAt = createdAt,
                order = order,
            )
    }
}
