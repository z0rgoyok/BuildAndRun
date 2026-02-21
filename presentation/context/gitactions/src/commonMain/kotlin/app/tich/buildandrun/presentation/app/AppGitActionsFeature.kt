package app.tich.buildandrun.presentation.app

interface AppGitActionsFeature {
    fun onPush(worktreePath: String)

    fun onPull(worktreePath: String)

    fun onCreatePullRequest(
        worktreePath: String,
        title: String,
        body: String,
        baseBranch: String?,
    )

    fun onOpenPullRequest(worktreePath: String)

    fun onLockWorktree(worktreePath: String)

    fun onUnlockWorktree(worktreePath: String)

    fun onRemoveWorktree(
        worktreePath: String,
        force: Boolean,
        deleteBranch: Boolean,
    )

    fun onCompleteWorktree(
        worktreePath: String,
        targetBranch: String,
        mergeIntoTarget: Boolean,
        pullTargetFirst: Boolean,
        deleteLocalBranch: Boolean,
        deleteRemoteBranch: Boolean,
        force: Boolean,
    )

    fun onLoadHasRemoteBranch(worktreePath: String)

    fun onPruneWorktrees()
}
