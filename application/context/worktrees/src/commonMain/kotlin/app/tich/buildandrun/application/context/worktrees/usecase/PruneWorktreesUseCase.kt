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

class PruneWorktreesUseCase(
    private val gitClient: GitClient,
    private val preferencesStore: PreferencesStore,
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

        return runCatchingCancellable {
            gitClient.pruneWorktrees(atRepoPath = repositoryPath)
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
    )

    data class Output(
        val worktrees: List<Worktree>,
    )
}
