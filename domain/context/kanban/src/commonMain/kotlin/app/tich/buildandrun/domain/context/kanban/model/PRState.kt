package app.tich.buildandrun.domain.context.kanban.model

enum class PRState {
    OPEN,
    CLOSED,
    MERGED,
    ;

    companion object {
        fun fromString(value: String): PRState =
            when (value.uppercase()) {
                "OPEN" -> OPEN
                "CLOSED" -> CLOSED
                "MERGED" -> MERGED
                else -> CLOSED
            }
    }
}
