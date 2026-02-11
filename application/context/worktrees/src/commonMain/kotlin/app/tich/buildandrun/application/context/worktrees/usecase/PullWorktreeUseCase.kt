package app.tich.buildandrun.application.context.worktrees.usecase

import app.tich.buildandrun.application.context.shared.path.normalizePath
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.domain.context.worktrees.model.WorktreeStatus
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

class PullWorktreeUseCase(
    private val gitClient: GitClient,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val worktreePath = normalizePath(input.worktreePath)
        if (worktreePath.isBlank()) {
            return UseCaseResult.Failure(
                value =
                    DomainFailure.Validation(
                        code = DomainFailureCode.APP_VALIDATION_WORKTREE_PATH_BLANK,
                        args = emptyList(),
                    ),
            )
        }

        return runCatchingCancellable {
            gitClient.pull(atWorktreePath = worktreePath)
            val updatedStatus = gitClient.getWorktreeStatus(atWorktreePath = worktreePath)
            UseCaseResult.Success(value = Output(status = updatedStatus))
        }.fold(
            onSuccess = { it },
            onFailure = { throwable ->
                throwable.toUseCaseFailure()
            },
        )
    }

    data class Input(val worktreePath: String)

    data class Output(
        val status: WorktreeStatus,
    )
}
