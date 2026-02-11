package app.tich.buildandrun.application.context.worktrees.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseResult
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId
import app.tich.buildandrun.domain.context.worktrees.model.Worktree

class CreateWorktreeFlowUseCase(
    private val createWorktreeUseCase: CreateWorktreeUseCase,
    private val copyConfiguredFilesUseCase: CopyConfiguredFilesUseCase,
    private val loadRepositoryWorktreesUseCase: LoadRepositoryWorktreesUseCase,
    private val loadBranchesUseCase: LoadBranchesUseCase,
    private val preferencesStore: PreferencesStore,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val createResult =
            when (
                val result =
                    createWorktreeUseCase.execute(
                        input =
                            CreateWorktreeUseCase.Input(
                                repositoryPath = input.repositoryPath,
                                branch = input.branch,
                                worktreePath = input.worktreePath,
                                createBranch = input.createBranch,
                                baseBranch = input.baseBranch,
                            ),
                    )
            ) {
                is UseCaseResult.Success -> result.value
                is UseCaseResult.Failure -> return result
            }

        when (
            val result =
                persistCreatedWorktreePreferences(
                    repositoryId = input.repositoryId,
                    worktreePath = createResult.createdWorktree.path,
                    baseBranch = input.baseBranch,
                )
        ) {
            is UseCaseResult.Success -> {
            }

            is UseCaseResult.Failure -> return result
        }

        copyConfiguredFilesUseCase.execute(
            input =
                CopyConfiguredFilesUseCase.Input(
                    repositoryPath = input.repositoryPath,
                    createdWorktreePath = createResult.createdWorktree.path,
                    repositoryId = input.repositoryId,
                ),
        )

        val worktreesResult =
            when (
                val result =
                    loadRepositoryWorktreesUseCase.execute(
                        input = LoadRepositoryWorktreesUseCase.Input(repositoryPath = input.repositoryPath),
                    )
            ) {
                is UseCaseResult.Success -> result.value
                is UseCaseResult.Failure -> return result
            }

        val branchesResult =
            when (
                val result =
                    loadBranchesUseCase.execute(
                        input = LoadBranchesUseCase.Input(repositoryPath = input.repositoryPath),
                    )
            ) {
                is UseCaseResult.Success -> result.value
                is UseCaseResult.Failure -> return result
            }

        return UseCaseResult.Success(
            value =
                Output(
                    createdWorktree = createResult.createdWorktree,
                    worktrees = worktreesResult.worktrees,
                    branches = branchesResult.branches,
                ),
        )
    }

    private fun persistCreatedWorktreePreferences(
        repositoryId: String,
        worktreePath: String,
        baseBranch: String,
    ): UseCaseResult<Unit> {
        return runCatchingCancellable {
            val normalizedBaseBranch = baseBranch.trim().ifBlank { null }
            if (normalizedBaseBranch != null) {
                preferencesStore.setPreferredBaseBranch(
                    branch = normalizedBaseBranch,
                    forRepositoryId = RepositoryId(repositoryId),
                )
                preferencesStore.setWorktreeBaseBranch(
                    branch = normalizedBaseBranch,
                    forWorktreePath = worktreePath,
                )
            }
            Unit
        }.toUseCaseResult()
    }

    data class Input(
        val repositoryId: String,
        val repositoryPath: String,
        val branch: String,
        val worktreePath: String,
        val createBranch: Boolean,
        val baseBranch: String,
    )

    data class Output(
        val createdWorktree: Worktree,
        val worktrees: List<Worktree>,
        val branches: List<String>,
    )
}
