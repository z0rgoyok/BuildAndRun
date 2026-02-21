package app.tich.buildandrun.application.context.worktrees.usecase

import app.tich.buildandrun.application.context.shared.path.normalizePath
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.domain.context.worktrees.model.Worktree
import app.tich.buildandrun.domain.context.worktrees.model.WorktreeStatus
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

class CreatePullRequestUseCase(
    private val gitClient: GitClient,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val worktreePath = normalizePath(input.worktree.path)
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
            val status = gitClient.getWorktreeStatus(atWorktreePath = worktreePath)
            if (status.hasUnpushedCommits || !status.hasRemote) {
                gitClient.push(
                    atWorktreePath = worktreePath,
                    setUpstream = !status.hasRemote,
                )
            }
            val pullRequestUrl =
                gitClient.createPR(
                    atWorktreePath = worktreePath,
                    title = input.title.ifBlank { input.worktree.branch },
                    body = input.body,
                    baseBranch = input.baseBranch,
                )
            val updatedStatus = gitClient.getWorktreeStatus(atWorktreePath = worktreePath)
            UseCaseResult.Success(
                value =
                    Output(
                        pullRequestUrl = pullRequestUrl,
                        status = updatedStatus,
                    ),
            )
        }.fold(
            onSuccess = { it },
            onFailure = { throwable ->
                throwable.toUseCaseFailure()
            },
        )
    }

    data class Input(
        val worktree: Worktree,
        val title: String,
        val body: String,
        val baseBranch: String?,
    )

    data class Output(
        val pullRequestUrl: String,
        val status: WorktreeStatus,
    )
}
