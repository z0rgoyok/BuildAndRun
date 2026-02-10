package app.tich.buildandrun.appstore

import app.tich.buildandrun.domain.failures.DomainFailure
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
