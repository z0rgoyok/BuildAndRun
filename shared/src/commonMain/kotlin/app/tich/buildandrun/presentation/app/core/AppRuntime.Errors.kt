package app.tich.buildandrun.presentation.app.core

import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.presentation.app.AppStore
import app.tich.buildandrun.presentation.errors.UiError

internal fun AppRuntime.mapFailureToErrorState(failure: DomainFailure): AppStore.ErrorState? {
    val uiError = failureToUiErrorMapper.map(failure) ?: return null
    return mapUiErrorToErrorState(uiError)
}

internal fun mapUiErrorToErrorState(uiError: UiError): AppStore.ErrorState {
    return AppStore.ErrorState(
        code = uiError.code,
        message = resolveText(text = uiError.message),
        details = uiError.details?.let { resolveText(text = it) },
        isRetryable = uiError.isRetryable,
    )
}
