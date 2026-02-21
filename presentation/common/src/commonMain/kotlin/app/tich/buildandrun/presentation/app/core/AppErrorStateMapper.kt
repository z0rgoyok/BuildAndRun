package app.tich.buildandrun.presentation.app.core

import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.presentation.app.ErrorState
import app.tich.buildandrun.presentation.errors.DomainFailureToUiErrorMapper
import app.tich.buildandrun.presentation.errors.UiError
import app.tich.buildandrun.presentation.i18n.UiText

class AppErrorStateMapper {
    private val failureToUiErrorMapper = DomainFailureToUiErrorMapper()

    fun mapFailureToErrorState(failure: DomainFailure): ErrorState? {
        val uiError = failureToUiErrorMapper.map(failure) ?: return null
        return mapUiErrorToErrorState(uiError)
    }

    fun mapUiErrorToErrorState(uiError: UiError): ErrorState =
        ErrorState(
            code = uiError.code,
            message = resolveText(text = uiError.message),
            details = uiError.details?.let { resolveText(text = it) },
            isRetryable = uiError.isRetryable,
        )
}

fun resolveText(text: UiText): String = app.tich.buildandrun.presentation.i18n.UiTextLocalizer.resolve(text = text)
