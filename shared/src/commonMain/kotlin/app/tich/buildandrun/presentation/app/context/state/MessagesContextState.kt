package app.tich.buildandrun.presentation.app.context.state

import app.tich.buildandrun.presentation.app.AppStore

internal class MessagesContextState {
    var error: AppStore.ErrorState? = null
    var success: AppStore.SuccessState? = null
}
