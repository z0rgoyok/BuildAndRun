package app.tich.buildandrun.domain.usecases

import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.failures.DomainFailureMapper
import app.tich.buildandrun.domain.ports.PreferencesStore

class LoadRepositoriesUseCase(
    private val preferencesStore: PreferencesStore,
) {
    suspend fun execute(): UseCaseResult<List<Repository>> =
        try {
            val repositories = preferencesStore.loadRepositories().sortedBy { it.name.lowercase() }
            UseCaseResult.Success(value = repositories)
        } catch (throwable: Throwable) {
            UseCaseResult.Failure(value = DomainFailureMapper.fromThrowable(throwable))
        }
}
