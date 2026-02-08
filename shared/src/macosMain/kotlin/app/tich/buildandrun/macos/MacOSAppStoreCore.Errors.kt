package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.failures.DomainFailure
import app.tich.buildandrun.presentation.errors.UiError

internal fun MacOSAppStoreCore.mapFailureToErrorState(failure: DomainFailure): MacOSAppStore.ErrorState? {
    val uiError = failureToUiErrorMapper.map(failure) ?: return null
    return mapUiErrorToErrorState(uiError)
}

internal fun MacOSAppStoreCore.mapUiErrorToErrorState(uiError: UiError): MacOSAppStore.ErrorState {
    return MacOSAppStore.ErrorState(
        code = uiError.code,
        message = resolveText(text = uiError.message),
        details = uiError.details?.let { resolveText(text = it) },
        isRetryable = uiError.isRetryable,
    )
}
