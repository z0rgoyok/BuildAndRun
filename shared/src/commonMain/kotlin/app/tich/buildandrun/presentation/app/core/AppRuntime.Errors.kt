package app.tich.buildandrun.presentation.app.core

import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.presentation.app.ErrorState
import app.tich.buildandrun.presentation.errors.UiError

internal fun AppRuntime.mapFailureToErrorState(failure: DomainFailure): ErrorState? {
    val uiError = failureToUiErrorMapper.map(failure) ?: return null
    return mapUiErrorToErrorState(uiError)
}

internal fun mapUiErrorToErrorState(uiError: UiError): ErrorState {
    return ErrorState(
        code = uiError.code,
        message = resolveText(text = uiError.message),
        details = uiError.details?.let { resolveText(text = it) },
        isRetryable = uiError.isRetryable,
    )
}
