package app.tich.buildandrun.application.context.worktrees.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.domain.context.worktrees.model.Worktree

class RemoveWorktreeUseCase(
    private val gitClient: GitClient,
    private val preferencesStore: PreferencesStore,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        return withValidatedWorktreeMutationInput(
            repositoryPath = input.repositoryPath,
            worktreePath = input.worktree.path,
            isMainWorktree = input.worktree.isMain,
        ) { normalizedInput ->
            val repositoryPath = normalizedInput.repositoryPath
            val worktreePath = normalizedInput.worktreePath
            runCatchingCancellable {
                val snapshot =
                    removeWorktreeAndLoadSnapshot(
                        gitClient = gitClient,
                        preferencesStore = preferencesStore,
                        repositoryPath = repositoryPath,
                        worktreePath = worktreePath,
                        branch = input.worktree.branch,
                        isDetachedHead = input.worktree.isDetachedHead,
                        force = input.force,
                        deleteLocalBranch = input.deleteBranch,
                        deleteRemoteBranch = false,
                    )
                UseCaseResult.Success(value = Output(worktrees = snapshot.worktrees, branches = snapshot.branches))
            }.fold(
                onSuccess = { it },
                onFailure = { throwable ->
                    throwable.toUseCaseFailure()
                },
            )
        }
    }

    data class Input(
        val repositoryPath: String,
        val worktree: Worktree,
        val force: Boolean,
        val deleteBranch: Boolean,
    )

    data class Output(
        val worktrees: List<Worktree>,
        val branches: List<String>,
    )
}
