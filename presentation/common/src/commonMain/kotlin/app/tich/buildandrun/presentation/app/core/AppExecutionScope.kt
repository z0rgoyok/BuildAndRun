package app.tich.buildandrun.presentation.app.core

import kotlinx.coroutines.*

class AppExecutionScope(
    onUnhandledError: (Throwable) -> Unit,
) {
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable -> onUnhandledError(throwable) }
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + exceptionHandler)

    fun destroy() {
        scope.cancel()
    }
}
