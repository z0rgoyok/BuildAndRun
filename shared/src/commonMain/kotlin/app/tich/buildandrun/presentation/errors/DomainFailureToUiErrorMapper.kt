package app.tich.buildandrun.presentation.errors

import app.tich.buildandrun.domain.failures.DomainFailure
import app.tich.buildandrun.domain.failures.DomainFailureCode
import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.resources.*
import org.jetbrains.compose.resources.StringResource

class DomainFailureToUiErrorMapper {
    fun map(failure: DomainFailure): UiError? =
        when (failure) {
            DomainFailure.Cancelled -> null
            is DomainFailure.Validation ->
                createUiError(
                    failure = failure,
                    kind = UiErrorKind.Validation,
                )
            is DomainFailure.NotFound ->
                createUiError(
                    failure = failure,
                    kind = UiErrorKind.NotFound,
                )
            is DomainFailure.Conflict ->
                createUiError(
                    failure = failure,
                    kind = UiErrorKind.Conflict,
                )
            is DomainFailure.PermissionDenied ->
                createUiError(
                    failure = failure,
                    kind = UiErrorKind.Permission,
                    primaryAction = UiError.PrimaryAction.OpenSettings,
                )
            is DomainFailure.ExternalTool ->
                createUiError(
                    failure = failure,
                    kind = UiErrorKind.ExternalTool,
                    primaryAction = if (failure.isRetryable) UiError.PrimaryAction.Retry else null,
                )
            is DomainFailure.Network ->
                createUiError(
                    failure = failure,
                    kind = UiErrorKind.Network,
                    primaryAction = if (failure.isRetryable) UiError.PrimaryAction.Retry else null,
                )
            is DomainFailure.Unknown ->
                createUiError(
                    failure = failure,
                    kind = UiErrorKind.Unknown,
                    primaryAction = if (failure.isRetryable) UiError.PrimaryAction.Retry else null,
                )
        }

    private fun createUiError(
        failure: DomainFailure,
        kind: UiErrorKind,
        primaryAction: UiError.PrimaryAction? = null,
    ): UiError {
        val args = failure.args
        val details = failure.details?.let { UiText(resource = Res.string.error_details, args = listOf(it)) }
        return UiError(
            code = failure.code,
            kind = kind,
            message = UiText(resource = codeToResource(failure.code), args = args),
            details = details,
            isRetryable = failure.isRetryable,
            primaryAction = primaryAction,
        )
    }

    private fun codeToResource(code: DomainFailureCode): StringResource =
        when (code) {
            DomainFailureCode.APP_CANCELLED -> Res.string.app_unknown
            DomainFailureCode.APP_CANNOT_REMOVE_MAIN_WORKTREE -> Res.string.app_cannot_remove_main_worktree
            DomainFailureCode.APP_GROUP_NAME_BLANK -> Res.string.app_group_name_blank
            DomainFailureCode.APP_GROUP_NAME_DUPLICATE -> Res.string.app_group_name_duplicate
            DomainFailureCode.APP_GROUP_NOT_FOUND -> Res.string.app_group_not_found
            DomainFailureCode.APP_INVALID_URL -> Res.string.app_invalid_url
            DomainFailureCode.APP_NO_EDITOR_CONFIGURED -> Res.string.app_no_editor_configured
            DomainFailureCode.APP_REPOSITORY_ALREADY_ADDED -> Res.string.app_repository_already_added
            DomainFailureCode.APP_REPOSITORY_NOT_FOUND -> Res.string.app_repository_not_found
            DomainFailureCode.APP_UNEXPECTED -> Res.string.app_unexpected
            DomainFailureCode.APP_UNKNOWN -> Res.string.app_unknown
            DomainFailureCode.APP_VALIDATION -> Res.string.app_validation
            DomainFailureCode.APP_VALIDATION_BRANCH_BLANK -> Res.string.app_validation_branch_blank
            DomainFailureCode.APP_VALIDATION_REPOSITORY_ID_BLANK -> Res.string.app_validation_repository_id_blank
            DomainFailureCode.APP_VALIDATION_REPOSITORY_PATH_BLANK -> Res.string.app_validation_repository_path_blank
            DomainFailureCode.APP_VALIDATION_TASK_TITLE_BLANK -> Res.string.app_validation_task_title_blank
            DomainFailureCode.APP_VALIDATION_WORKTREE_PATH_BLANK -> Res.string.app_validation_worktree_path_blank
            DomainFailureCode.GIT_BRANCH_ALREADY_EXISTS -> Res.string.git_branch_already_exists
            DomainFailureCode.GIT_BRANCH_NOT_FOUND -> Res.string.git_branch_not_found
            DomainFailureCode.GIT_CANNOT_REMOVE_MAIN_WORKTREE -> Res.string.git_cannot_remove_main_worktree
            DomainFailureCode.GIT_COMMAND_FAILED -> Res.string.git_command_failed
            DomainFailureCode.GIT_INVALID_PATH -> Res.string.git_invalid_path
            DomainFailureCode.GIT_MERGE_CONFLICT -> Res.string.git_merge_conflict
            DomainFailureCode.GIT_NOT_A_REPOSITORY -> Res.string.git_not_a_repository
            DomainFailureCode.GIT_PR_CREATION_FAILED -> Res.string.git_pr_creation_failed
            DomainFailureCode.GIT_WORKTREE_ALREADY_EXISTS -> Res.string.git_worktree_already_exists
            DomainFailureCode.GIT_WORKTREE_HAS_UNCOMMITTED_CHANGES -> Res.string.git_worktree_has_uncommitted_changes
            DomainFailureCode.GIT_WORKTREE_NOT_FOUND -> Res.string.git_worktree_not_found
        }
}
