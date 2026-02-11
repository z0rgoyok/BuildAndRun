package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

class SetPreferredBaseBranchUseCase(
    private val preferencesStore: PreferencesStore,
) {
    fun execute(input: Input): UseCaseResult<Output> {
        val normalizedBranch = input.branch.trim()
        if (normalizedBranch.isBlank()) {
            return UseCaseResult.Failure(
                value =
                    DomainFailure.Validation(
                        code = DomainFailureCode.APP_VALIDATION_BRANCH_BLANK,
                        args = emptyList(),
                    ),
            )
        }

        return runCatchingCancellable {
            preferencesStore.setPreferredBaseBranch(
                branch = normalizedBranch,
                forRepositoryId = RepositoryId(input.repositoryId),
            )
            UseCaseResult.Success(value = Output(branch = normalizedBranch))
        }.fold(
            onSuccess = { it },
            onFailure = { throwable -> throwable.toUseCaseFailure() },
        )
    }

    data class Input(
        val repositoryId: String,
        val branch: String,
    )

    data class Output(
        val branch: String,
    )
}
