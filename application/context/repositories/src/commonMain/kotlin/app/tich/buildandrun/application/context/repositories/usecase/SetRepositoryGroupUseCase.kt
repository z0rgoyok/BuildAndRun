package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.context.repositories.model.RepositoryGroupId
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode
import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper

class SetRepositoryGroupUseCase(
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
                val groupId = input.groupId?.trim()?.takeIf { it.isNotBlank() }?.let { RepositoryGroupId(it) }
                val updatedRepository = repositories[repositoryIndex].copy(groupId = groupId)
                val updatedRepositories = repositories.toMutableList().apply { this[repositoryIndex] = updatedRepository }
                preferencesStore.saveRepositories(repositories = updatedRepositories)
                UseCaseResult.Success(Output(repositories = updatedRepositories))
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
        val groupId: String?,
    )

    data class Output(
        val repositories: List<Repository>,
    )
}
