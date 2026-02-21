package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.domain.context.repositories.model.RepositoryGroup
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

class RenameRepositoryGroupUseCase(
    private val preferencesStore: PreferencesStore,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val trimmedName = input.newName.trim()
        if (trimmedName.isBlank()) {
            return blankNameFailure()
        }
        val groupIndex = input.currentGroups.indexOfFirst { it.id.value == input.groupId }
        if (groupIndex == -1) {
            return groupNotFoundFailure()
        }
        if (input.currentGroups.any { it.id.value != input.groupId && it.name.equals(trimmedName, ignoreCase = true) }) {
            return duplicateNameFailure(trimmedName)
        }

        return runCatchingCancellable {
            val groups =
                input.currentGroups.toMutableList().apply {
                    this[groupIndex] = this[groupIndex].copy(name = trimmedName)
                }
            preferencesStore.saveRepositoryGroups(groups)
            UseCaseResult.Success(value = Output(groups = groups))
        }.fold(
            onSuccess = { it },
            onFailure = { throwable -> throwable.toUseCaseFailure() },
        )
    }

    data class Input(
        val groupId: String,
        val newName: String,
        val currentGroups: List<RepositoryGroup>,
    )

    data class Output(
        val groups: List<RepositoryGroup>,
    )

    private fun blankNameFailure(): UseCaseResult.Failure =
        UseCaseResult.Failure(
            value =
                DomainFailure.Validation(
                    code = DomainFailureCode.APP_GROUP_NAME_BLANK,
                    args = emptyList(),
                ),
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
