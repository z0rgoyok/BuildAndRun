package app.tich.buildandrun.presentation.app.context.gitactions.impl

import app.tich.buildandrun.presentation.app.AppGitActionsFeature
import app.tich.buildandrun.presentation.app.core.AppWiring

class AppGitActionsService(
    private val runtime: AppWiring,
) : AppGitActionsFeature {
    override fun onPush(worktreePath: String) {
        runtime.onPush(worktreePath = worktreePath)
    }

    override fun onPull(worktreePath: String) {
        runtime.onPull(worktreePath = worktreePath)
    }

    override fun onCreatePullRequest(
        worktreePath: String,
        title: String,
        body: String,
        baseBranch: String?,
    ) {
        runtime.onCreatePullRequest(
            worktreePath = worktreePath,
            title = title,
            body = body,
            baseBranch = baseBranch,
        )
    }

    override fun onOpenPullRequest(worktreePath: String) {
        runtime.onOpenPullRequest(worktreePath = worktreePath)
    }

    override fun onLockWorktree(worktreePath: String) {
        runtime.onLockWorktree(worktreePath = worktreePath)
    }

    override fun onUnlockWorktree(worktreePath: String) {
        runtime.onUnlockWorktree(worktreePath = worktreePath)
    }

    override fun onRemoveWorktree(
        worktreePath: String,
        force: Boolean,
        deleteBranch: Boolean,
    ) {
        runtime.onRemoveWorktree(
            worktreePath = worktreePath,
            force = force,
            deleteBranch = deleteBranch,
        )
    }

    override fun onCompleteWorktree(
        worktreePath: String,
        targetBranch: String,
        mergeIntoTarget: Boolean,
        pullTargetFirst: Boolean,
        deleteLocalBranch: Boolean,
        deleteRemoteBranch: Boolean,
        force: Boolean,
    ) {
        runtime.onCompleteWorktree(
            worktreePath = worktreePath,
            targetBranch = targetBranch,
            mergeIntoTarget = mergeIntoTarget,
            pullTargetFirst = pullTargetFirst,
            deleteLocalBranch = deleteLocalBranch,
            deleteRemoteBranch = deleteRemoteBranch,
            force = force,
        )
    }

    override fun onLoadHasRemoteBranch(worktreePath: String) {
        runtime.onLoadHasRemoteBranch(worktreePath = worktreePath)
    }

    override fun onPruneWorktrees() {
        runtime.onPruneWorktrees()
    }
}
