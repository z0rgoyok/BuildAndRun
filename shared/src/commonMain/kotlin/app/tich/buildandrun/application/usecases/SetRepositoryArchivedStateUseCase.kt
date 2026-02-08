package app.tich.buildandrun.application.usecases

import app.tich.buildandrun.application.ports.PreferencesStore
import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.failures.DomainFailure
import app.tich.buildandrun.domain.failures.DomainFailureCode
import app.tich.buildandrun.domain.failures.DomainFailureMapper

class SetRepositoryArchivedStateUseCase(
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
            val repositoryIndex = repositories.indexOfFirst { it.id.value == repositoryId }
            if (repositoryIndex == -1) {
                UseCaseResult.Failure(
                    DomainFailure.NotFound(
                        code = DomainFailureCode.APP_REPOSITORY_NOT_FOUND,
                        args = listOf(repositoryId),
                        isRetryable = false,
                    ),
                )
            } else {
                val updatedRepository = repositories[repositoryIndex].copy(isArchived = input.isArchived)
                val updatedRepositories = repositories.toMutableList().apply { this[repositoryIndex] = updatedRepository }
                preferencesStore.saveRepositories(repositories = updatedRepositories)
                UseCaseResult.Success(
                    value =
                        Output(
                            repositories = updatedRepositories,
                            updatedRepository = updatedRepository,
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

    data class Input(
        val repositoryId: String,
        val isArchived: Boolean,
    )

    data class Output(
        val repositories: List<Repository>,
        val updatedRepository: Repository,
    )
}
