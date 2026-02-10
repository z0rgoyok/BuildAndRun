package app.tich.buildandrun.appstore

internal class AppMessagesService(
    private val runtime: AppRuntime,
) : AppMessagesFeature {
    override fun onDismissError() {
        runtime.onDismissError()
    }

    override fun onDismissSuccess() {
        runtime.onDismissSuccess()
    }
}
