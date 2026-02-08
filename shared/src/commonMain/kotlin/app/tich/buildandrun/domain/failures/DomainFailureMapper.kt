package app.tich.buildandrun.domain.failures

import app.tich.buildandrun.domain.errors.AppError
import app.tich.buildandrun.domain.errors.GitError

object DomainFailureMapper {
    fun fromThrowable(throwable: Throwable): DomainFailure =
        when (throwable) {
            is AppError.Validation ->
                DomainFailure.Validation(
                    code =
                        when (throwable.reason) {
                            AppError.ValidationReason.WORKTREE_PATH_BLANK -> DomainFailureCode.APP_VALIDATION_WORKTREE_PATH_BLANK
                            AppError.ValidationReason.REPOSITORY_PATH_BLANK -> DomainFailureCode.APP_VALIDATION_REPOSITORY_PATH_BLANK
                            AppError.ValidationReason.REPOSITORY_ID_BLANK -> DomainFailureCode.APP_VALIDATION_REPOSITORY_ID_BLANK
                            AppError.ValidationReason.BRANCH_BLANK -> DomainFailureCode.APP_VALIDATION_BRANCH_BLANK
                            AppError.ValidationReason.TASK_TITLE_BLANK -> DomainFailureCode.APP_VALIDATION_TASK_TITLE_BLANK
                        },
                    args = emptyList(),
                )
            is AppError.RepositoryAlreadyAdded ->
                DomainFailure.Conflict(
                    code = DomainFailureCode.APP_REPOSITORY_ALREADY_ADDED,
                    args = emptyList(),
                    isRetryable = false,
                )
            is AppError.NoEditorConfigured ->
                DomainFailure.Validation(
                    code = DomainFailureCode.APP_NO_EDITOR_CONFIGURED,
                    args = emptyList(),
                )
            is AppError.InvalidURL ->
                DomainFailure.Validation(
                    code = DomainFailureCode.APP_INVALID_URL,
                    args = listOf(throwable.urlString),
                )
            is AppError.CannotRemoveMainWorktree ->
                DomainFailure.Conflict(
                    code = DomainFailureCode.APP_CANNOT_REMOVE_MAIN_WORKTREE,
                    args = emptyList(),
                    isRetryable = false,
                )
            is AppError.Cancelled -> DomainFailure.Cancelled
            is AppError.Unexpected ->
                DomainFailure.Unknown(
                    code = DomainFailureCode.APP_UNEXPECTED,
                    args = emptyList(),
                    isRetryable = true,
                    details = throwable.message,
                )
            is GitError.NotARepository ->
                DomainFailure.Validation(
                    code = DomainFailureCode.GIT_NOT_A_REPOSITORY,
                    args = listOf(throwable.path),
                )
            is GitError.WorktreeAlreadyExists ->
                DomainFailure.Conflict(
                    code = DomainFailureCode.GIT_WORKTREE_ALREADY_EXISTS,
                    args = listOf(throwable.name),
                    isRetryable = false,
                )
            is GitError.WorktreeNotFound ->
                DomainFailure.NotFound(
                    code = DomainFailureCode.GIT_WORKTREE_NOT_FOUND,
                    args = listOf(throwable.path),
                    isRetryable = false,
                )
            is GitError.BranchAlreadyExists ->
                DomainFailure.Conflict(
                    code = DomainFailureCode.GIT_BRANCH_ALREADY_EXISTS,
                    args = listOf(throwable.name),
                    isRetryable = false,
                )
            is GitError.BranchNotFound ->
                DomainFailure.NotFound(
                    code = DomainFailureCode.GIT_BRANCH_NOT_FOUND,
                    args = listOf(throwable.name),
                    isRetryable = false,
                )
            GitError.CannotRemoveMainWorktree ->
                DomainFailure.Conflict(
                    code = DomainFailureCode.GIT_CANNOT_REMOVE_MAIN_WORKTREE,
                    args = emptyList(),
                    isRetryable = false,
                )
            is GitError.CommandFailed ->
                DomainFailure.ExternalTool(
                    code = DomainFailureCode.GIT_COMMAND_FAILED,
                    args = emptyList(),
                    isRetryable = true,
                    details = throwable.errorMessage,
                )
            is GitError.InvalidPath ->
                DomainFailure.Validation(
                    code = DomainFailureCode.GIT_INVALID_PATH,
                    args = listOf(throwable.path),
                )
            is GitError.WorktreeHasUncommittedChanges ->
                DomainFailure.Conflict(
                    code = DomainFailureCode.GIT_WORKTREE_HAS_UNCOMMITTED_CHANGES,
                    args = listOf(throwable.path),
                    isRetryable = false,
                )
            is GitError.PRCreationFailed ->
                DomainFailure.ExternalTool(
                    code = DomainFailureCode.GIT_PR_CREATION_FAILED,
                    args = emptyList(),
                    isRetryable = true,
                    details = throwable.reason,
                )
            is GitError.MergeConflict ->
                DomainFailure.Conflict(
                    code = DomainFailureCode.GIT_MERGE_CONFLICT,
                    args = listOf(throwable.source, throwable.target),
                    isRetryable = false,
                )
            else ->
                DomainFailure.Unknown(
                    code = DomainFailureCode.APP_UNKNOWN,
                    args = emptyList(),
                    isRetryable = true,
                    details = throwable.message,
                )
        }
}
