package app.tich.buildandrun.domain.failures

import app.tich.buildandrun.domain.errors.AppError
import app.tich.buildandrun.domain.errors.GitError

object DomainFailureMapper {
    fun fromThrowable(throwable: Throwable): DomainFailure =
        when (throwable) {
            is AppError.Validation ->
                DomainFailure.Validation(
                    code = "app.validation",
                    reason = throwable.reason,
                    payload = mapOf("reason" to throwable.reason),
                )
            AppError.RepositoryAlreadyAdded ->
                DomainFailure.Conflict(
                    code = "app.repository_already_added",
                    payload = emptyMap(),
                    isRetryable = false,
                )
            AppError.NoEditorConfigured ->
                DomainFailure.Validation(
                    code = "app.no_editor_configured",
                    reason = "no_editor_configured",
                    payload = mapOf("reason" to "no_editor_configured"),
                )
            is AppError.InvalidURL ->
                DomainFailure.Validation(
                    code = "app.invalid_url",
                    reason = throwable.urlString,
                    payload = mapOf("url" to throwable.urlString),
                )
            AppError.CannotRemoveMainWorktree ->
                DomainFailure.Conflict(
                    code = "app.cannot_remove_main_worktree",
                    payload = emptyMap(),
                    isRetryable = false,
                )
            AppError.Cancelled -> DomainFailure.Cancelled
            is AppError.Unexpected ->
                DomainFailure.Unknown(
                    code = "app.unexpected",
                    payload = throwable.messagePayload(),
                    isRetryable = true,
                )
            is GitError.NotARepository ->
                DomainFailure.Validation(
                    code = "git.not_a_repository",
                    reason = throwable.path,
                    payload = mapOf("path" to throwable.path),
                )
            is GitError.WorktreeAlreadyExists ->
                DomainFailure.Conflict(
                    code = "git.worktree_already_exists",
                    payload = mapOf("name" to throwable.name),
                    isRetryable = false,
                )
            is GitError.WorktreeNotFound ->
                DomainFailure.NotFound(
                    code = "git.worktree_not_found",
                    payload = mapOf("path" to throwable.path),
                    isRetryable = false,
                )
            is GitError.BranchAlreadyExists ->
                DomainFailure.Conflict(
                    code = "git.branch_already_exists",
                    payload = mapOf("name" to throwable.name),
                    isRetryable = false,
                )
            is GitError.BranchNotFound ->
                DomainFailure.NotFound(
                    code = "git.branch_not_found",
                    payload = mapOf("name" to throwable.name),
                    isRetryable = false,
                )
            GitError.CannotRemoveMainWorktree ->
                DomainFailure.Conflict(
                    code = "git.cannot_remove_main_worktree",
                    payload = emptyMap(),
                    isRetryable = false,
                )
            is GitError.CommandFailed ->
                DomainFailure.ExternalTool(
                    code = "git.command_failed",
                    payload = mapOf("details" to throwable.errorMessage),
                    isRetryable = true,
                )
            is GitError.InvalidPath ->
                DomainFailure.Validation(
                    code = "git.invalid_path",
                    reason = throwable.path,
                    payload = mapOf("path" to throwable.path),
                )
            is GitError.WorktreeHasUncommittedChanges ->
                DomainFailure.Conflict(
                    code = "git.worktree_has_uncommitted_changes",
                    payload = mapOf("path" to throwable.path),
                    isRetryable = false,
                )
            is GitError.PRCreationFailed ->
                DomainFailure.ExternalTool(
                    code = "git.pr_creation_failed",
                    payload = mapOf("details" to throwable.reason),
                    isRetryable = true,
                )
            is GitError.MergeConflict ->
                DomainFailure.Conflict(
                    code = "git.merge_conflict",
                    payload =
                        mapOf(
                            "source" to throwable.source,
                            "target" to throwable.target,
                        ),
                    isRetryable = false,
                )
            else ->
                DomainFailure.Unknown(
                    code = "app.unknown",
                    payload = throwable.messagePayload(),
                    isRetryable = true,
                )
        }

    private fun Throwable.messagePayload(): Map<String, String> {
        val messageValue = message ?: return emptyMap()
        return mapOf("details" to messageValue)
    }
}
