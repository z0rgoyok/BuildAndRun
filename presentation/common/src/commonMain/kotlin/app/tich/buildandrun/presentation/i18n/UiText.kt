package app.tich.buildandrun.presentation.i18n

import org.jetbrains.compose.resources.StringResource

data class UiText(
    val resource: StringResource,
    val args: List<String> = emptyList(),
    val quantity: Int? = null,
)
