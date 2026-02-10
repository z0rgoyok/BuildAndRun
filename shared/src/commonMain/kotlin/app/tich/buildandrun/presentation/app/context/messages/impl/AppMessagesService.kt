package app.tich.buildandrun.presentation.app.context.messages.impl

import app.tich.buildandrun.presentation.app.AppMessagesFeature
import app.tich.buildandrun.presentation.app.core.AppRuntime
import app.tich.buildandrun.presentation.app.core.onDismissError
import app.tich.buildandrun.presentation.app.core.onDismissSuccess

internal class AppMessagesService(
    private val runtime: AppRuntime,
) : AppMessagesFeature {
    override fun onDismissError() {
        runtime.onDismissError()
    }

    override fun onDismissSuccess() {
        runtime.onDismissSuccess()
    }
}
