package app.tich.buildandrun.appstore

internal class AppWorktreesService(
    private val runtime: AppRuntime,
) : AppWorktreesFeature {
    override fun onSelectWorktree(worktreePath: String?) {
        runtime.onSelectWorktree(worktreePath = worktreePath)
    }

    override fun onRefreshSelectedRepository() {
        runtime.onRefreshSelectedRepository()
    }

    override fun onRefreshWorktreeStatus(worktreePath: String) {
        runtime.onRefreshWorktreeStatus(worktreePath = worktreePath)
    }

    override fun onCreateWorktreeBranchChanged(value: String) {
        runtime.onCreateWorktreeBranchChanged(value = value)
    }

    override fun onCreateWorktreePathChanged(value: String) {
        runtime.onCreateWorktreePathChanged(value = value)
    }

    override fun onCreateWorktreeBaseBranchChanged(value: String) {
        runtime.onCreateWorktreeBaseBranchChanged(value = value)
    }

    override fun onCreateWorktreeCreateBranchChanged(value: Boolean) {
        runtime.onCreateWorktreeCreateBranchChanged(value = value)
    }

    override fun onCreateWorktree() {
        runtime.onCreateWorktree()
    }
}
