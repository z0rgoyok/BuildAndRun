package app.tich.buildandrun.domain.errors

sealed class AppError(
    override val message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    enum class ValidationReason {
        WORKTREE_PATH_BLANK,
        REPOSITORY_PATH_BLANK,
        REPOSITORY_ID_BLANK,
        BRANCH_BLANK,
        TASK_TITLE_BLANK,
    }

    data class Validation(
        val reason: ValidationReason,
    ) : AppError("Validation error: $reason")

    class RepositoryAlreadyAdded : AppError(
        "This repository has already been added",
    )

    class NoEditorConfigured : AppError(
        "No editor is configured",
    )

    data class InvalidURL(val urlString: String) : AppError(
        "Invalid URL: $urlString",
    )

    class CannotRemoveMainWorktree : AppError(
        "The main worktree cannot be removed",
    )

    class Cancelled : AppError(
        "Operation was cancelled",
    )

    data class Unexpected(
        val reason: String,
        private val originalCause: Throwable? = null,
    ) : AppError("Unexpected error: $reason", originalCause)
}
