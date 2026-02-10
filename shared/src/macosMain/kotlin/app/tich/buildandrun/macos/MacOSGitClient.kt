package app.tich.buildandrun.macos

import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.domain.context.worktrees.model.WorktreeStatus

class MacOSGitClient : GitClient {
    override suspend fun getRepositoryRoot(atPath: String): String = macOsGitGetRepositoryRoot(atPath = atPath)

    override suspend fun listBranches(atRepoPath: String): List<String> = macOsGitListBranches(atRepoPath = atRepoPath)

    override suspend fun branchExists(
        atRepoPath: String,
        branch: String,
    ): Boolean = macOsGitBranchExists(atRepoPath = atRepoPath, branch = branch)

    override suspend fun deleteBranch(
        atRepoPath: String,
        branch: String,
        force: Boolean,
    ) = macOsGitDeleteBranch(atRepoPath = atRepoPath, branch = branch, force = force)

    override suspend fun listWorktrees(atRepoPath: String) = macOsGitListWorktrees(atRepoPath = atRepoPath)

    override suspend fun createWorktree(
        atRepoPath: String,
        worktreePath: String,
        branch: String,
        createBranch: Boolean,
        baseBranch: String?,
    ) = macOsGitCreateWorktree(
        atRepoPath = atRepoPath,
        worktreePath = worktreePath,
        branch = branch,
        createBranch = createBranch,
        baseBranch = baseBranch,
    )

    override suspend fun removeWorktree(
        atRepoPath: String,
        worktreePath: String,
        force: Boolean,
    ) = macOsGitRemoveWorktree(
        atRepoPath = atRepoPath,
        worktreePath = worktreePath,
        force = force,
    )

    override suspend fun lockWorktree(
        atRepoPath: String,
        worktreePath: String,
        reason: String?,
    ) = macOsGitLockWorktree(
        atRepoPath = atRepoPath,
        worktreePath = worktreePath,
        reason = reason,
    )

    override suspend fun unlockWorktree(
        atRepoPath: String,
        worktreePath: String,
    ) = macOsGitUnlockWorktree(
        atRepoPath = atRepoPath,
        worktreePath = worktreePath,
    )

    override suspend fun pruneWorktrees(atRepoPath: String) = macOsGitPruneWorktrees(atRepoPath = atRepoPath)

    override suspend fun getWorktreeStatus(atWorktreePath: String): WorktreeStatus =
        macOsGitGetWorktreeStatus(atWorktreePath = atWorktreePath)

    override suspend fun push(
        atWorktreePath: String,
        setUpstream: Boolean,
    ) = macOsGitPush(
        atWorktreePath = atWorktreePath,
        setUpstream = setUpstream,
    )

    override suspend fun pull(atWorktreePath: String) = macOsGitPull(atWorktreePath = atWorktreePath)

    override suspend fun createPR(
        atWorktreePath: String,
        title: String,
        body: String,
        baseBranch: String?,
    ): String =
        macOsGitCreatePr(
            atWorktreePath = atWorktreePath,
            title = title,
            body = body,
            baseBranch = baseBranch,
        )

    override suspend fun mergeBranch(
        atRepoPath: String,
        source: String,
        intoTarget: String,
    ) = macOsGitMergeBranch(
        atRepoPath = atRepoPath,
        source = source,
        intoTarget = intoTarget,
    )

    override suspend fun deleteRemoteBranch(
        atRepoPath: String,
        branch: String,
    ) = macOsGitDeleteRemoteBranch(
        atRepoPath = atRepoPath,
        branch = branch,
    )

    override suspend fun hasRemoteBranch(
        atRepoPath: String,
        branch: String,
    ): Boolean =
        macOsGitHasRemoteBranch(
            atRepoPath = atRepoPath,
            branch = branch,
        )
}
