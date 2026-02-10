package app.tich.buildandrun.application.context.worktrees.usecase

import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult

import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.domain.context.worktrees.model.Worktree
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode
import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper

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

        return runCatching {
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
                UseCaseResult.Failure(value = DomainFailureMapper.fromThrowable(throwable))
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

    private fun normalizePath(path: String): String = path.trim().trimEnd('/')
}
