package app.tich.buildandrun.presentation.app.context.state

import app.tich.buildandrun.presentation.app.ErrorState
import app.tich.buildandrun.presentation.app.SuccessState

class MessagesContextState {
    var error: ErrorState? = null
    var success: SuccessState? = null
}
