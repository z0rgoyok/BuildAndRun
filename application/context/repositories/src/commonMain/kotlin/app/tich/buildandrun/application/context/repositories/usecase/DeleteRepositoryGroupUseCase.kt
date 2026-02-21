package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.context.repositories.model.RepositoryGroup
import app.tich.buildandrun.domain.context.repositories.model.RepositoryGroupId
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

class DeleteRepositoryGroupUseCase(
    private val preferencesStore: PreferencesStore,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val groupId = input.groupId.trim()
        if (groupId.isBlank()) {
            return groupNotFoundFailure()
        }
        if (input.currentGroups.none { it.id.value == groupId }) {
            return groupNotFoundFailure()
        }

        return runCatchingCancellable {
            val removedGroupId = RepositoryGroupId(groupId)
            val groups = input.currentGroups.filter { it.id.value != groupId }
            val repositories =
                input.currentRepositories.map { repository ->
                    if (repository.groupId == removedGroupId) {
                        repository.copy(groupId = null)
                    } else {
                        repository
                    }
                }
            preferencesStore.saveRepositoryGroups(groups)
            preferencesStore.saveRepositories(repositories)
            UseCaseResult.Success(value = Output(groups = groups, repositories = repositories))
        }.fold(
            onSuccess = { it },
            onFailure = { throwable -> throwable.toUseCaseFailure() },
        )
    }

    data class Input(
        val groupId: String,
        val currentGroups: List<RepositoryGroup>,
        val currentRepositories: List<Repository>,
    )

    data class Output(
        val groups: List<RepositoryGroup>,
        val repositories: List<Repository>,
    )

    private fun groupNotFoundFailure(): UseCaseResult.Failure =
        UseCaseResult.Failure(
            value =
                DomainFailure.NotFound(
                    code = DomainFailureCode.APP_GROUP_NOT_FOUND,
                    args = emptyList(),
                    isRetryable = false,
                ),
        )
}
