package app.tich.buildandrun.macos

internal fun MacOSAppStoreCore.clearMessages() {
    error = null
    success = null
}

internal fun MacOSAppStoreCore.onDismissError() {
    error = null
    publishState()
}

internal fun MacOSAppStoreCore.onDismissSuccess() {
    success = null
    publishState()
}
