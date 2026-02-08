package app.tich.buildandrun.appstore

internal fun AppStoreCore.clearMessages() {
    error = null
    success = null
}

internal fun AppStoreCore.onDismissError() {
    error = null
    publishState()
}

internal fun AppStoreCore.onDismissSuccess() {
    success = null
    publishState()
}
