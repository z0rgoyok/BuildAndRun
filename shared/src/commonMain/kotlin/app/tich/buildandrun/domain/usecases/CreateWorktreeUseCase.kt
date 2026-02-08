package app.tich.buildandrun.domain.usecases

import app.tich.buildandrun.domain.entities.Worktree
import app.tich.buildandrun.domain.failures.DomainFailure
import app.tich.buildandrun.domain.failures.DomainFailureMapper
import app.tich.buildandrun.domain.ports.GitClient

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
                    code = "app.validation.repository_path_blank",
                    reason = "repository_path_blank",
                    payload = mapOf("reason" to "repository_path_blank"),
                ),
            )
        }

        if (branch.isBlank()) {
            return UseCaseResult.Failure(
                DomainFailure.Validation(
                    code = "app.validation.branch_blank",
                    reason = "branch_blank",
                    payload = mapOf("reason" to "branch_blank"),
                ),
            )
        }

        if (worktreePath.isBlank()) {
            return UseCaseResult.Failure(
                DomainFailure.Validation(
                    code = "app.validation.worktree_path_blank",
                    reason = "worktree_path_blank",
                    payload = mapOf("reason" to "worktree_path_blank"),
                ),
            )
        }

        return try {
            gitClient.createWorktree(
                atRepoPath = repositoryPath,
                worktreePath = worktreePath,
                branch = branch,
                createBranch = input.createBranch,
                baseBranch = input.baseBranch?.trim()?.ifBlank { null },
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
                        baseBranch = input.baseBranch?.trim()?.ifBlank { null },
                    )

            UseCaseResult.Success(
                value =
                    Output(
                        createdWorktree = createdWorktree,
                        allWorktrees = worktrees,
                    ),
            )
        } catch (throwable: Throwable) {
            UseCaseResult.Failure(value = DomainFailureMapper.fromThrowable(throwable))
        }
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
