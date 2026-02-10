package app.tich.buildandrun.presentation.app.context.worktrees.impl

interface WorktreesOperations {
    fun loadWorktreesForRepository(path: String)

    fun onRefreshWorktreeStatus(worktreePath: String)

    suspend fun loadWorktreesForRepositoryInternal(path: String)
}
