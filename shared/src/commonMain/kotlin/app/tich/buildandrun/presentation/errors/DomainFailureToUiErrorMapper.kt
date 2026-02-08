package app.tich.buildandrun.presentation.errors

import app.tich.buildandrun.domain.failures.DomainFailure
import app.tich.buildandrun.presentation.i18n.UiText

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
        val args = payloadToOrderedArgs(payload = failure.payload)
        val details = failure.payload["details"]?.let { UiText.Key(key = "error.details", args = listOf(it)) }
        return UiError(
            code = failure.code,
            kind = kind,
            message = UiText.Key(key = failure.code, args = args),
            details = details,
            isRetryable = failure.isRetryable,
            primaryAction = primaryAction,
        )
    }

    private fun payloadToOrderedArgs(payload: Map<String, String>): List<String> =
        payload.entries
            .sortedBy { it.key }
            .map { it.value }
}
