package app.tich.buildandrun.presentation.errors

import app.tich.buildandrun.presentation.i18n.UiText

data class UiError(
    val code: String,
    val kind: UiErrorKind,
    val message: UiText,
    val details: UiText?,
    val isRetryable: Boolean,
    val primaryAction: PrimaryAction?,
) {
    enum class PrimaryAction {
        Retry,
        OpenSettings,
        RevealInFinder,
    }
}
