package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper

class LoadRepositoriesUseCase(
    private val preferencesStore: PreferencesStore,
) {
    suspend fun execute(): UseCaseResult<List<Repository>> =
        runCatching {
            val repositories = preferencesStore.loadRepositories().sortedBy { it.name.lowercase() }
            UseCaseResult.Success(value = repositories)
        }.fold(
            onSuccess = { it },
            onFailure = { throwable ->
                UseCaseResult.Failure(value = DomainFailureMapper.fromThrowable(throwable))
            },
        )
}
