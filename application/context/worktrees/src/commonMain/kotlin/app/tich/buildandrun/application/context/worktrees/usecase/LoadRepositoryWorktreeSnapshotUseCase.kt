package app.tich.buildandrun.application.context.worktrees.usecase

import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.worktrees.model.Worktree
import app.tich.buildandrun.domain.context.worktrees.model.WorktreeStatus

class LoadRepositoryWorktreeSnapshotUseCase(
    private val loadRepositoryWorktreesUseCase: LoadRepositoryWorktreesUseCase,
    private val loadWorktreeStatusUseCase: LoadWorktreeStatusUseCase,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val worktrees =
            when (
                val worktreesResult =
                    loadRepositoryWorktreesUseCase.execute(
                        input = LoadRepositoryWorktreesUseCase.Input(repositoryPath = input.repositoryPath),
                    )
            ) {
                is UseCaseResult.Success -> worktreesResult.value.worktrees
                is UseCaseResult.Failure -> return worktreesResult
            }

        val statusesByPath = linkedMapOf<String, WorktreeStatus>()
        worktrees.forEach { worktree ->
            when (
                val statusResult =
                    loadWorktreeStatusUseCase.execute(
                        input = LoadWorktreeStatusUseCase.Input(worktreePath = worktree.path),
                    )
            ) {
                is UseCaseResult.Success -> {
                    statusesByPath[worktree.path] = statusResult.value.status
                }

                is UseCaseResult.Failure -> {
                    return statusResult
                }
            }
        }

        return UseCaseResult.Success(
            value =
                Output(
                    worktrees = worktrees,
                    worktreeStatusesByPath = statusesByPath,
                ),
        )
    }

    data class Input(
        val repositoryPath: String,
    )

    data class Output(
        val worktrees: List<Worktree>,
        val worktreeStatusesByPath: Map<String, WorktreeStatus>,
    )
}
