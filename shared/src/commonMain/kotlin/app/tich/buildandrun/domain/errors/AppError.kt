package app.tich.buildandrun.domain.errors

sealed class AppError(
    override val message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    data class Validation(val reason: String) : AppError(
        "Validation error: $reason",
    )

    data object RepositoryAlreadyAdded : AppError(
        "This repository has already been added",
    )

    data object NoEditorConfigured : AppError(
        "No editor is configured",
    )

    data class InvalidURL(val urlString: String) : AppError(
        "Invalid URL: $urlString",
    )

    data object CannotRemoveMainWorktree : AppError(
        "The main worktree cannot be removed",
    )

    data object Cancelled : AppError(
        "Operation was cancelled",
    )

    data class Unexpected(
        val reason: String,
        private val originalCause: Throwable? = null,
    ) : AppError("Unexpected error: $reason", originalCause)
}
