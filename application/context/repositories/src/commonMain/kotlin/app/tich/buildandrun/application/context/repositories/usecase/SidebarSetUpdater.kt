package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure

internal fun updateSidebarSet(
    current: Set<String>,
    value: String,
    enabled: Boolean,
): Set<String> {
    return if (enabled) {
        current + value
    } else {
        current - value
    }
}

internal fun persistSidebarSetUpdate(
    current: Set<String>,
    next: Set<String>,
    persist: (Set<String>) -> Unit,
): UseCaseResult<Set<String>> {
    if (next == current) {
        return UseCaseResult.Success(value = next)
    }
    return runCatchingCancellable {
        persist(next)
        UseCaseResult.Success(value = next)
    }.fold(
        onSuccess = { it },
        onFailure = { throwable -> throwable.toUseCaseFailure() },
    )
}
