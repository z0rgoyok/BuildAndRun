package app.tich.buildandrun.presentation.app

data class KanbanState(
    val kanbanTasks: List<KanbanTaskItem> = emptyList(),
)
