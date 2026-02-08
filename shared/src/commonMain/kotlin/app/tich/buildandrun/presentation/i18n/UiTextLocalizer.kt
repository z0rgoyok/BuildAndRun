package app.tich.buildandrun.presentation.i18n

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

object UiTextLocalizer {
    fun resolve(text: UiText): String =
        runBlocking {
            getString(
                resource = text.resource,
                *text.args.toTypedArray(),
            )
        }
}
