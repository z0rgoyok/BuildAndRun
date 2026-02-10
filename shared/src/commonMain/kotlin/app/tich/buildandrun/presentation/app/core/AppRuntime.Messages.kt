package app.tich.buildandrun.presentation.app.core

internal fun AppRuntime.clearMessages() {
    messagesState.error = null
    messagesState.success = null
}

internal fun AppRuntime.onDismissError() {
    messagesState.error = null
    publishState()
}

internal fun AppRuntime.onDismissSuccess() {
    messagesState.success = null
    publishState()
}
