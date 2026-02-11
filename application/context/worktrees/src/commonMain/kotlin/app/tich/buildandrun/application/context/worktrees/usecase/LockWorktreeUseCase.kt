package app.tich.buildandrun.application.context.worktrees.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.path.normalizePath
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.domain.context.worktrees.model.Worktree
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

class LockWorktreeUseCase(
    private val gitClient: GitClient,
    private val preferencesStore: PreferencesStore,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val repositoryPath = normalizePath(input.repositoryPath)
        val worktreePath = normalizePath(input.worktreePath)
        if (repositoryPath.isBlank()) {
            return blankRepositoryFailure()
        }
        if (worktreePath.isBlank()) {
            return blankWorktreeFailure()
        }

        return runCatchingCancellable {
            gitClient.lockWorktree(
                atRepoPath = repositoryPath,
                worktreePath = worktreePath,
                reason = null,
            )
            val worktrees =
                gitClient.listWorktrees(atRepoPath = repositoryPath).map { worktree ->
                    val baseBranch = preferencesStore.worktreeBaseBranch(forWorktreePath = worktree.path)
                    worktree.withBaseBranch(baseBranch = baseBranch)
                }
            UseCaseResult.Success(value = Output(worktrees = worktrees))
        }.fold(
            onSuccess = { it },
            onFailure = { throwable ->
                throwable.toUseCaseFailure()
            },
        )
    }

    data class Input(
        val repositoryPath: String,
        val worktreePath: String,
    )

    data class Output(
        val worktrees: List<Worktree>,
    )

    private fun blankRepositoryFailure(): UseCaseResult.Failure =
        UseCaseResult.Failure(
            value =
                DomainFailure.Validation(
                    code = DomainFailureCode.APP_VALIDATION_REPOSITORY_PATH_BLANK,
                    args = emptyList(),
                ),
        )

    private fun blankWorktreeFailure(): UseCaseResult.Failure =
        UseCaseResult.Failure(
            value =
                DomainFailure.Validation(
                    code = DomainFailureCode.APP_VALIDATION_WORKTREE_PATH_BLANK,
                    args = emptyList(),
                ),
        )
}
