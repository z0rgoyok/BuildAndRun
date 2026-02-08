package app.tich.buildandrun.application.usecases

import app.tich.buildandrun.application.ports.PreferencesStore
import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.failures.DomainFailure
import app.tich.buildandrun.domain.failures.DomainFailureCode
import app.tich.buildandrun.domain.failures.DomainFailureMapper

class RemoveRepositoryUseCase(
    private val preferencesStore: PreferencesStore,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val repositoryId = input.repositoryId.trim()
        if (repositoryId.isBlank()) {
            return UseCaseResult.Failure(
                DomainFailure.Validation(
                    code = DomainFailureCode.APP_VALIDATION_REPOSITORY_ID_BLANK,
                    args = emptyList(),
                ),
            )
        }

        return runCatching {
            val repositories = preferencesStore.loadRepositories()
            val removedRepository = repositories.firstOrNull { it.id.value == repositoryId }
            if (removedRepository == null) {
                UseCaseResult.Failure(
                    DomainFailure.NotFound(
                        code = DomainFailureCode.APP_REPOSITORY_NOT_FOUND,
                        args = listOf(repositoryId),
                        isRetryable = false,
                    ),
                )
            } else {
                val updatedRepositories = repositories.filterNot { it.id.value == repositoryId }
                preferencesStore.saveRepositories(repositories = updatedRepositories)
                UseCaseResult.Success(
                    value =
                        Output(
                            repositories = updatedRepositories,
                            removedRepository = removedRepository,
                        ),
                )
            }
        }.fold(
            onSuccess = { it },
            onFailure = { throwable ->
                UseCaseResult.Failure(value = DomainFailureMapper.fromThrowable(throwable))
            },
        )
    }

    data class Input(val repositoryId: String)

    data class Output(
        val repositories: List<Repository>,
        val removedRepository: Repository,
    )
}
