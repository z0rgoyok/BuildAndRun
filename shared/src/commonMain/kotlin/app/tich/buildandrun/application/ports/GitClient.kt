package app.tich.buildandrun.application.ports

import app.tich.buildandrun.domain.entities.Worktree
import app.tich.buildandrun.domain.entities.WorktreeStatus

interface GitClient {
    suspend fun getRepositoryRoot(atPath: String): String

    suspend fun listBranches(atRepoPath: String): List<String>

    suspend fun branchExists(
        atRepoPath: String,
        branch: String,
    ): Boolean

    suspend fun deleteBranch(
        atRepoPath: String,
        branch: String,
        force: Boolean = false,
    )

    suspend fun listWorktrees(atRepoPath: String): List<Worktree>

    suspend fun createWorktree(
        atRepoPath: String,
        worktreePath: String,
        branch: String,
        createBranch: Boolean,
        baseBranch: String?,
    )

    suspend fun removeWorktree(
        atRepoPath: String,
        worktreePath: String,
        force: Boolean = false,
    )

    suspend fun lockWorktree(
        atRepoPath: String,
        worktreePath: String,
        reason: String? = null,
    )

    suspend fun unlockWorktree(
        atRepoPath: String,
        worktreePath: String,
    )

    suspend fun pruneWorktrees(atRepoPath: String)

    suspend fun getWorktreeStatus(atWorktreePath: String): WorktreeStatus

    suspend fun push(
        atWorktreePath: String,
        setUpstream: Boolean = false,
    )

    suspend fun pull(atWorktreePath: String)

    suspend fun createPR(
        atWorktreePath: String,
        title: String,
        body: String,
        baseBranch: String?,
    ): String

    suspend fun mergeBranch(
        atRepoPath: String,
        source: String,
        intoTarget: String,
    )

    suspend fun deleteRemoteBranch(
        atRepoPath: String,
        branch: String,
    )

    suspend fun hasRemoteBranch(
        atRepoPath: String,
        branch: String,
    ): Boolean
}
