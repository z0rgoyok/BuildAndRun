package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure

class ToggleSidebarRepositoriesExpansionUseCase(
    private val preferencesStore: PreferencesStore,
) {
    fun execute(input: Input): UseCaseResult<Output> {
        if (input.repositoryIds.isEmpty()) {
            return UseCaseResult.Success(value = Output(expandedRepositoryIds = input.currentExpandedRepositoryIds))
        }

        val areAllExpanded = input.repositoryIds.all(input.currentExpandedRepositoryIds::contains)
        val next =
            if (areAllExpanded) {
                val updated = (input.currentExpandedRepositoryIds - input.repositoryIds).toMutableSet()
                val preferred = input.preferredRepositoryId?.takeIf { input.repositoryIds.contains(it) }
                if (preferred != null) {
                    updated.add(preferred)
                }
                updated.toSet()
            } else {
                input.currentExpandedRepositoryIds + input.repositoryIds
            }

        if (next == input.currentExpandedRepositoryIds) {
            return UseCaseResult.Success(value = Output(expandedRepositoryIds = next))
        }

        return runCatchingCancellable {
            preferencesStore.expandedRepositoryIds = next
            UseCaseResult.Success(value = Output(expandedRepositoryIds = next))
        }.fold(
            onSuccess = { it },
            onFailure = { throwable -> throwable.toUseCaseFailure() },
        )
    }

    data class Input(
        val repositoryIds: Set<String>,
        val preferredRepositoryId: String?,
        val currentExpandedRepositoryIds: Set<String>,
    )

    data class Output(
        val expandedRepositoryIds: Set<String>,
    )
}
