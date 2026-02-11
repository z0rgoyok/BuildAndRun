package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

class LoadPreferredBaseBranchUseCase(
    private val preferencesStore: PreferencesStore,
) {
    fun execute(input: Input): UseCaseResult<Output> {
        val repositoryId = input.repositoryId.trim()
        if (repositoryId.isBlank()) {
            return repositoryIdBlankFailure()
        }

        return runCatchingCancellable {
            val branch = preferencesStore.preferredBaseBranch(forRepositoryId = RepositoryId(repositoryId))
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

    private fun repositoryIdBlankFailure(): UseCaseResult.Failure =
        UseCaseResult.Failure(
            value =
                DomainFailure.Validation(
                    code = DomainFailureCode.APP_VALIDATION_REPOSITORY_ID_BLANK,
                    args = emptyList(),
                ),
        )
}
