package app.tich.buildandrun.presentation.app.context.worktrees.impl.usecase

interface CopyConfiguredFilesUseCase {
    suspend fun execute(
        repositoryPath: String,
        createdWorktreePath: String,
        repositoryId: String,
    )
}
