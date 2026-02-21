package app.tich.buildandrun.application.context.worktrees.usecase

import app.tich.buildandrun.application.context.shared.path.normalizePath
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.domain.context.worktrees.model.Worktree
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

class CreateWorktreeUseCase(
    private val gitClient: GitClient,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val repositoryPath = normalizePath(input.repositoryPath)
        val branch = input.branch.trim()
        val worktreePath = normalizePath(input.worktreePath)

        if (repositoryPath.isBlank()) {
            return UseCaseResult.Failure(
                DomainFailure.Validation(
                    code = DomainFailureCode.APP_VALIDATION_REPOSITORY_PATH_BLANK,
                    args = emptyList(),
                ),
            )
        }

        if (branch.isBlank()) {
            return UseCaseResult.Failure(
                DomainFailure.Validation(
                    code = DomainFailureCode.APP_VALIDATION_BRANCH_BLANK,
                    args = emptyList(),
                ),
            )
        }

        if (worktreePath.isBlank()) {
            return UseCaseResult.Failure(
                DomainFailure.Validation(
                    code = DomainFailureCode.APP_VALIDATION_WORKTREE_PATH_BLANK,
                    args = emptyList(),
                ),
            )
        }

        return runCatchingCancellable {
            val normalizedBaseBranch = input.baseBranch?.trim()?.ifBlank { null }
            gitClient.createWorktree(
                atRepoPath = repositoryPath,
                worktreePath = worktreePath,
                branch = branch,
                createBranch = input.createBranch,
                baseBranch = normalizedBaseBranch,
            )
            val worktrees = gitClient.listWorktrees(atRepoPath = repositoryPath)
            val createdWorktree =
                worktrees.firstOrNull { normalizePath(it.path) == worktreePath }
                    ?: Worktree(
                        path = worktreePath,
                        branch = branch,
                        isMain = false,
                        commitHash = null,
                        isLocked = false,
                        isPrunable = false,
                        baseBranch = normalizedBaseBranch,
                    )

            UseCaseResult.Success(
                value =
                    Output(
                        createdWorktree = createdWorktree,
                        allWorktrees = worktrees,
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
        val repositoryPath: String,
        val branch: String,
        val worktreePath: String,
        val createBranch: Boolean = true,
        val baseBranch: String? = null,
    )

    data class Output(
        val createdWorktree: Worktree,
        val allWorktrees: List<Worktree>,
    )
}
