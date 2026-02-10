package app.tich.buildandrun.presentation.app

interface AppWorktreesFeature {
    fun onSelectWorktree(worktreePath: String?)

    fun onRefreshSelectedRepository()

    fun onRefreshWorktreeStatus(worktreePath: String)

    fun onCreateWorktreeBranchChanged(value: String)

    fun onCreateWorktreePathChanged(value: String)

    fun onCreateWorktreeBaseBranchChanged(value: String)

    fun onCreateWorktreeCreateBranchChanged(value: Boolean)

    fun onCreateWorktree()
}
