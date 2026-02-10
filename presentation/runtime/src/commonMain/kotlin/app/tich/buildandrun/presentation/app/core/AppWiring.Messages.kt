package app.tich.buildandrun.presentation.app.core

fun AppWiring.clearMessages() {
    messagesState.error = null
    messagesState.success = null
}

fun AppWiring.onDismissError() {
    messagesState.error = null
    publishState()
}

fun AppWiring.onDismissSuccess() {
    messagesState.success = null
    publishState()
}
