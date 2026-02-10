package app.tich.buildandrun.appstore

internal fun AppRuntime.clearMessages() {
    error = null
    success = null
}

internal fun AppRuntime.onDismissError() {
    error = null
    publishState()
}

internal fun AppRuntime.onDismissSuccess() {
    success = null
    publishState()
}
