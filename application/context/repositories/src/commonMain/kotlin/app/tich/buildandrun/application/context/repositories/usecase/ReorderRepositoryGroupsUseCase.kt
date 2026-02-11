package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.domain.context.repositories.model.RepositoryGroup

class ReorderRepositoryGroupsUseCase(
    private val preferencesStore: PreferencesStore,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        return runCatchingCancellable {
            val groupById = input.currentGroups.associateBy { it.id.value }
            val reordered = input.orderedGroupIds.mapIndexedNotNull { index, id -> groupById[id]?.copy(sortOrder = index) }
            val missingGroups = input.currentGroups.filter { it.id.value !in input.orderedGroupIds }
            val groups = reordered + missingGroups
            preferencesStore.saveRepositoryGroups(groups)
            UseCaseResult.Success(value = Output(groups = groups))
        }.fold(
            onSuccess = { it },
            onFailure = { throwable -> throwable.toUseCaseFailure() },
        )
    }

    data class Input(
        val currentGroups: List<RepositoryGroup>,
        val orderedGroupIds: List<String>,
    )

    data class Output(
        val groups: List<RepositoryGroup>,
    )
}
