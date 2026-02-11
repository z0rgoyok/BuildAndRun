package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId

class PersistCreatedWorktreePreferencesUseCase(
    private val preferencesStore: PreferencesStore,
) {
    fun execute(input: Input): UseCaseResult<Output> {
        return runCatchingCancellable {
            val normalizedBaseBranch = input.baseBranch.trim().ifBlank { null }
            if (normalizedBaseBranch != null) {
                preferencesStore.setPreferredBaseBranch(
                    branch = normalizedBaseBranch,
                    forRepositoryId = RepositoryId(input.repositoryId),
                )
                preferencesStore.setWorktreeBaseBranch(
                    branch = normalizedBaseBranch,
                    forWorktreePath = input.worktreePath,
                )
            }
            UseCaseResult.Success(value = Output(baseBranch = normalizedBaseBranch))
        }.fold(
            onSuccess = { it },
            onFailure = { throwable ->
                throwable.toUseCaseFailure()
            },
        )
    }

    data class Input(
        val repositoryId: String,
        val worktreePath: String,
        val baseBranch: String,
    )

    data class Output(
        val baseBranch: String?,
    )
}
