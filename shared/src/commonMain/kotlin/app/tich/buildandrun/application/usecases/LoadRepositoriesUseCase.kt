package app.tich.buildandrun.application.usecases

import app.tich.buildandrun.application.ports.PreferencesStore
import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.failures.DomainFailureMapper

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
