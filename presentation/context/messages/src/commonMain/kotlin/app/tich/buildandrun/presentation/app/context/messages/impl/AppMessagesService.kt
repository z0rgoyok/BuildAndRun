package app.tich.buildandrun.presentation.app.context.messages.impl

import app.tich.buildandrun.presentation.app.AppMessagesFeature
import app.tich.buildandrun.presentation.app.core.AppWiring
import app.tich.buildandrun.presentation.app.core.onDismissError
import app.tich.buildandrun.presentation.app.core.onDismissSuccess

class AppMessagesService(
    private val runtime: AppWiring,
) : AppMessagesFeature {
    override fun onDismissError() {
        runtime.onDismissError()
    }

    override fun onDismissSuccess() {
        runtime.onDismissSuccess()
    }
}
