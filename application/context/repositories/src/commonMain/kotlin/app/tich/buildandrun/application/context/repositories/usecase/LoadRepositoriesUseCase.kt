package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.domain.context.repositories.model.Repository

class LoadRepositoriesUseCase(
    private val preferencesStore: PreferencesStore,
) {
    suspend fun execute(): UseCaseResult<List<Repository>> =
        runCatchingCancellable {
            val repositories = preferencesStore.loadRepositories().sortedBy { it.name.lowercase() }
            UseCaseResult.Success(value = repositories)
        }.fold(
            onSuccess = { it },
            onFailure = { throwable ->
                throwable.toUseCaseFailure()
            },
        )
}
