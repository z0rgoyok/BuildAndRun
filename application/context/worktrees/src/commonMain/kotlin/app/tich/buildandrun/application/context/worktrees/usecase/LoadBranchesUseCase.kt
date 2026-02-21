package app.tich.buildandrun.application.context.worktrees.usecase

import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

class LoadBranchesUseCase(
    private val gitClient: GitClient,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val repositoryPath = input.repositoryPath.trim()
        if (repositoryPath.isBlank()) {
            return UseCaseResult.Failure(
                value =
                    DomainFailure.Validation(
                        code = DomainFailureCode.APP_VALIDATION_REPOSITORY_PATH_BLANK,
                        args = emptyList(),
                    ),
            )
        }
        return runCatchingCancellable {
            val branches = gitClient.listBranches(atRepoPath = repositoryPath)
            UseCaseResult.Success(
                value = Output(branches = branches),
            )
        }.fold(
            onSuccess = { it },
            onFailure = { throwable -> throwable.toUseCaseFailure() },
        )
    }

    data class Input(
        val repositoryPath: String,
    )

    data class Output(
        val branches: List<String>,
    )
}
