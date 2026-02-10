package app.tich.buildandrun.domain.context.kanban.model

enum class KanbanColumnType(
    val displayName: String,
    val icon: String,
    val accentColor: String,
) {
    TODO(
        displayName = "To Do",
        icon = "circle",
        accentColor = "gray",
    ),
    IN_PROGRESS(
        displayName = "In Progress",
        icon = "circle.lefthalf.filled",
        accentColor = "blue",
    ),
    REVIEW(
        displayName = "Review",
        icon = "eye",
        accentColor = "orange",
    ),
    DONE(
        displayName = "Done",
        icon = "checkmark.circle.fill",
        accentColor = "green",
    ),
    ;

    companion object {
        fun fromString(value: String): KanbanColumnType =
            when (value.uppercase().replace(" ", "_")) {
                "TODO", "TO_DO" -> TODO
                "IN_PROGRESS" -> IN_PROGRESS
                "REVIEW" -> REVIEW
                "DONE" -> DONE
                else -> TODO
            }
    }
}
