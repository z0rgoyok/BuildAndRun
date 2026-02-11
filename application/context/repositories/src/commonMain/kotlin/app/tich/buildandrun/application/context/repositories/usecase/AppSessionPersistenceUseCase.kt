package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure

class AppSessionPersistenceUseCase(
    private val preferencesStore: PreferencesStore,
) {
    fun execute(input: Input): UseCaseResult<Output> {
        return runCatchingCancellable {
            preferencesStore.lastSelectedRepositoryId = input.repositoryId
            preferencesStore.lastSelectedWorktreePath = input.worktreePath
            UseCaseResult.Success(value = Output)
        }.fold(
            onSuccess = { it },
            onFailure = { throwable ->
                throwable.toUseCaseFailure()
            },
        )
    }

    data class Input(
        val repositoryId: String?,
        val worktreePath: String?,
    )

    data object Output
}
