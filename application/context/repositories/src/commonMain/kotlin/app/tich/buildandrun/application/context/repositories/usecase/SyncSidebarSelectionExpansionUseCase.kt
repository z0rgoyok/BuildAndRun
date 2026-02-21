package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure

class SyncSidebarSelectionExpansionUseCase(
    private val preferencesStore: PreferencesStore,
) {
    fun execute(input: Input): UseCaseResult<Output> {
        val selectedRepositoryId =
            input.repositoryId?.trim()?.takeIf(String::isNotBlank)
                ?: return UseCaseResult.Success(value = Output(expandedRepositoryIds = input.currentExpandedRepositoryIds, changed = false))
        if (input.currentExpandedRepositoryIds.contains(selectedRepositoryId)) {
            return UseCaseResult.Success(value = Output(expandedRepositoryIds = input.currentExpandedRepositoryIds, changed = false))
        }

        val next = input.currentExpandedRepositoryIds + selectedRepositoryId
        return runCatchingCancellable {
            preferencesStore.expandedRepositoryIds = next
            UseCaseResult.Success(value = Output(expandedRepositoryIds = next, changed = true))
        }.fold(
            onSuccess = { it },
            onFailure = { throwable -> throwable.toUseCaseFailure() },
        )
    }

    data class Input(
        val repositoryId: String?,
        val currentExpandedRepositoryIds: Set<String>,
    )

    data class Output(
        val expandedRepositoryIds: Set<String>,
        val changed: Boolean,
    )
}
