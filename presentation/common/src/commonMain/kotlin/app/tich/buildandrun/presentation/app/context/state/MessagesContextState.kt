package app.tich.buildandrun.presentation.app.context.state

import app.tich.buildandrun.presentation.app.ErrorState
import app.tich.buildandrun.presentation.app.MessagesState
import app.tich.buildandrun.presentation.app.SuccessState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

class MessagesContextState {
    private val mutableState = MutableValue(MessagesState())

    var error: ErrorState? = null
    var success: SuccessState? = null

    val state: Value<MessagesState> = mutableState

    fun clear() {
        error = null
        success = null
    }

    fun dismissError() {
        error = null
        publish()
    }

    fun dismissSuccess() {
        success = null
        publish()
    }

    fun publish() {
        mutableState.value = MessagesState(error = error, success = success)
    }
}
