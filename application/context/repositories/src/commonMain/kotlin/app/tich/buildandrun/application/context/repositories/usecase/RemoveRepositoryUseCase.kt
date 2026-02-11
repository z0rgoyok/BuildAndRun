package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

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

        return runCatchingCancellable {
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
                throwable.toUseCaseFailure()
            },
        )
    }

    data class Input(val repositoryId: String)

    data class Output(
        val repositories: List<Repository>,
        val removedRepository: Repository,
    )
}
