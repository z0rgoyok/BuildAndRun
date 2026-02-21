package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.domain.context.repositories.model.RepositoryGroup
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

class CreateRepositoryGroupUseCase(
    private val preferencesStore: PreferencesStore,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val trimmedName = input.name.trim()
        if (trimmedName.isBlank()) {
            return blankNameFailure()
        }
        if (input.currentGroups.any { it.name.equals(trimmedName, ignoreCase = true) }) {
            return duplicateNameFailure(trimmedName)
        }

        return runCatchingCancellable {
            val nextSortOrder = (input.currentGroups.maxOfOrNull { it.sortOrder } ?: -1) + 1
            val group = RepositoryGroup.create(name = trimmedName, sortOrder = nextSortOrder)
            val groups = input.currentGroups + group
            preferencesStore.saveRepositoryGroups(groups)
            UseCaseResult.Success(value = Output(groups = groups, createdGroup = group))
        }.fold(
            onSuccess = { it },
            onFailure = { throwable -> throwable.toUseCaseFailure() },
        )
    }

    data class Input(
        val name: String,
        val currentGroups: List<RepositoryGroup>,
    )

    data class Output(
        val groups: List<RepositoryGroup>,
        val createdGroup: RepositoryGroup,
    )

    private fun blankNameFailure(): UseCaseResult.Failure =
        UseCaseResult.Failure(
            value =
                DomainFailure.Validation(
                    code = DomainFailureCode.APP_GROUP_NAME_BLANK,
                    args = emptyList(),
                ),
        )

    private fun duplicateNameFailure(name: String): UseCaseResult.Failure =
        UseCaseResult.Failure(
            value =
                DomainFailure.Conflict(
                    code = DomainFailureCode.APP_GROUP_NAME_DUPLICATE,
                    args = listOf(name),
                    isRetryable = false,
                ),
        )
}
