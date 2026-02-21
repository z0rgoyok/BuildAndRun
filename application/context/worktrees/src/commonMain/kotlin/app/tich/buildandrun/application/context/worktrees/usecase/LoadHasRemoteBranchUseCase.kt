package app.tich.buildandrun.application.context.worktrees.usecase

import app.tich.buildandrun.application.context.shared.path.normalizePath
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

class LoadHasRemoteBranchUseCase(
    private val gitClient: GitClient,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val repositoryPath = normalizePath(input.repositoryPath)
        if (repositoryPath.isBlank()) {
            return UseCaseResult.Failure(
                value =
                    DomainFailure.Validation(
                        code = DomainFailureCode.APP_VALIDATION_REPOSITORY_PATH_BLANK,
                        args = emptyList(),
                    ),
            )
        }
        val branch = input.branch.trim()
        if (branch.isBlank()) {
            return UseCaseResult.Success(value = Output(hasRemoteBranch = false))
        }

        return runCatchingCancellable {
            val hasRemoteBranch =
                gitClient.hasRemoteBranch(
                    atRepoPath = repositoryPath,
                    branch = branch,
                )
            UseCaseResult.Success(value = Output(hasRemoteBranch = hasRemoteBranch))
        }.fold(
            onSuccess = { it },
            onFailure = { throwable ->
                throwable.toUseCaseFailure()
            },
        )
    }

    data class Input(
        val repositoryPath: String,
        val branch: String,
    )

    data class Output(
        val hasRemoteBranch: Boolean,
    )
}
