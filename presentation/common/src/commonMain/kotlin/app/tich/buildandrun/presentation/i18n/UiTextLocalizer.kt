package app.tich.buildandrun.presentation.i18n

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

object UiTextLocalizer {
    private var resolverOverride: ((UiText) -> String)? = null

    fun resolve(text: UiText): String =
        resolverOverride?.invoke(text) ?: runBlocking {
            getString(
                resource = text.resource,
                *text.args.toTypedArray(),
            )
        }

    fun setResolverOverride(resolver: ((UiText) -> String)?) {
        resolverOverride = resolver
    }
}
