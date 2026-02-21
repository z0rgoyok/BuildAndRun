package app.tich.buildandrun.presentation.app.context.messages.impl

import app.tich.buildandrun.presentation.app.AppMessagesFeature
import app.tich.buildandrun.presentation.app.context.state.MessagesContextState

class AppMessagesService(
    private val messagesState: MessagesContextState,
) : AppMessagesFeature {
    override fun onDismissError() {
        messagesState.dismissError()
    }

    override fun onDismissSuccess() {
        messagesState.dismissSuccess()
    }
}
