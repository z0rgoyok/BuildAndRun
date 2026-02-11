package app.tich.buildandrun.application.context.shared.usecase

import kotlin.coroutines.cancellation.CancellationException

inline fun <T> runCatchingCancellable(block: () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (throwable: Throwable) {
        if (throwable is CancellationException) {
            throw throwable
        }
        Result.failure(throwable)
    }
}
