package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId

class LoadPreferredBaseBranchUseCase(
    private val preferencesStore: PreferencesStore,
) {
    fun execute(input: Input): UseCaseResult<Output> {
        return runCatchingCancellable {
            val branch = preferencesStore.preferredBaseBranch(forRepositoryId = RepositoryId(input.repositoryId))
            UseCaseResult.Success(value = Output(branch = branch))
        }.fold(
            onSuccess = { it },
            onFailure = { throwable -> throwable.toUseCaseFailure() },
        )
    }

    data class Input(
        val repositoryId: String,
    )

    data class Output(
        val branch: String?,
    )
}
