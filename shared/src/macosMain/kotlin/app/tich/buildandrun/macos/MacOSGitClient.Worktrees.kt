package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.entities.Worktree
import app.tich.buildandrun.domain.entities.WorktreeStatus
import app.tich.buildandrun.domain.errors.GitError

internal suspend fun macOsGitListWorktrees(atRepoPath: String): List<Worktree> {
    val repoPath = macOsGitNormalizePath(atRepoPath)
    val result = macOsGitRunCommand(listOf("git", "-C", repoPath, "worktree", "list", "--porcelain"))
    if (result.exitCode != 0) {
        throw macOsGitMapCommandError(
            result = result,
            repoPath = repoPath,
            branch = null,
            worktreePath = null,
        )
    }
    return macOsGitParseWorktrees(
        repoPath = repoPath,
        output = result.output,
    )
}

internal suspend fun macOsGitCreateWorktree(
    atRepoPath: String,
    worktreePath: String,
    branch: String,
    createBranch: Boolean,
    baseBranch: String?,
) {
    val repoPath = macOsGitNormalizePath(atRepoPath)
    val branchName = branch.trim()
    val targetWorktreePath = macOsGitNormalizePath(worktreePath)
    if (repoPath.isBlank()) {
        throw GitError.InvalidPath(path = atRepoPath)
    }
    if (targetWorktreePath.isBlank()) {
        throw GitError.InvalidPath(path = worktreePath)
    }
    if (branchName.isBlank()) {
        throw GitError.BranchNotFound(name = branch)
    }
    val command =
        buildList {
            addAll(listOf("git", "-C", repoPath, "worktree", "add"))
            if (createBranch) {
                add("-b")
                add(branchName)
                add(targetWorktreePath)
                baseBranch?.trim()?.takeIf(String::isNotBlank)?.let(::add)
            } else {
                add(targetWorktreePath)
                add(branchName)
            }
        }
    val result = macOsGitRunCommand(command)
    if (result.exitCode != 0) {
        throw macOsGitMapCommandError(
            result = result,
            repoPath = repoPath,
            branch = branchName,
            worktreePath = targetWorktreePath,
        )
    }
}

internal suspend fun macOsGitRemoveWorktree(
    atRepoPath: String,
    worktreePath: String,
    force: Boolean,
) {
    val repoPath = macOsGitNormalizePath(atRepoPath)
    val targetWorktreePath = macOsGitNormalizePath(worktreePath)
    if (repoPath.isBlank()) {
        throw GitError.InvalidPath(path = atRepoPath)
    }
    if (targetWorktreePath.isBlank()) {
        throw GitError.InvalidPath(path = worktreePath)
    }
    val command =
        buildList {
            addAll(listOf("git", "-C", repoPath, "worktree", "remove"))
            if (force) {
                add("--force")
            }
            add(targetWorktreePath)
        }
    val result = macOsGitRunCommand(command)
    if (result.exitCode != 0) {
        throw macOsGitMapCommandError(
            result = result,
            repoPath = repoPath,
            branch = null,
            worktreePath = targetWorktreePath,
        )
    }
}

internal suspend fun macOsGitLockWorktree(
    atRepoPath: String,
    worktreePath: String,
    reason: String?,
) {
    val repoPath = macOsGitNormalizePath(atRepoPath)
    val targetWorktreePath = macOsGitNormalizePath(worktreePath)
    val command =
        buildList {
            addAll(listOf("git", "-C", repoPath, "worktree", "lock"))
            reason?.trim()?.takeIf(String::isNotBlank)?.let {
                add("--reason")
                add(it)
            }
            add(targetWorktreePath)
        }
    val result = macOsGitRunCommand(command)
    if (result.exitCode != 0) {
        throw macOsGitMapCommandError(
            result = result,
            repoPath = repoPath,
            branch = null,
            worktreePath = targetWorktreePath,
        )
    }
}

internal suspend fun macOsGitUnlockWorktree(
    atRepoPath: String,
    worktreePath: String,
) {
    val repoPath = macOsGitNormalizePath(atRepoPath)
    val targetWorktreePath = macOsGitNormalizePath(worktreePath)
    val result =
        macOsGitRunCommand(
            listOf(
                "git",
                "-C",
                repoPath,
                "worktree",
                "unlock",
                targetWorktreePath,
            ),
        )
    if (result.exitCode != 0) {
        throw macOsGitMapCommandError(
            result = result,
            repoPath = repoPath,
            branch = null,
            worktreePath = targetWorktreePath,
        )
    }
}

internal suspend fun macOsGitPruneWorktrees(atRepoPath: String) {
    val repoPath = macOsGitNormalizePath(atRepoPath)
    val result = macOsGitRunCommand(listOf("git", "-C", repoPath, "worktree", "prune"))
    if (result.exitCode != 0) {
        throw macOsGitMapCommandError(
            result = result,
            repoPath = repoPath,
            branch = null,
            worktreePath = null,
        )
    }
}

internal suspend fun macOsGitGetWorktreeStatus(atWorktreePath: String): WorktreeStatus {
    val worktreePath = macOsGitNormalizePath(atWorktreePath)
    if (worktreePath.isBlank()) {
        throw GitError.InvalidPath(path = atWorktreePath)
    }
    val statusResult = macOsGitRunCommand(listOf("git", "-C", worktreePath, "status", "--porcelain"))
    if (statusResult.exitCode != 0) {
        throw macOsGitMapCommandError(
            result = statusResult,
            repoPath = worktreePath,
            branch = null,
            worktreePath = worktreePath,
        )
    }
    val isDirty = statusResult.output.isNotBlank()

    val branchResult = macOsGitRunCommand(listOf("git", "-C", worktreePath, "branch", "--show-current"))
    if (branchResult.exitCode != 0) {
        throw macOsGitMapCommandError(
            result = branchResult,
            repoPath = worktreePath,
            branch = null,
            worktreePath = worktreePath,
        )
    }
    val branch = branchResult.output.lineSequence().firstOrNull()?.trim().orEmpty()
    if (branch.isBlank()) {
        return WorktreeStatus(
            isDirty = isDirty,
            hasRemote = false,
            ahead = 0,
            behind = 0,
            prStatus = null,
        )
    }

    val trackingResult =
        macOsGitRunCommand(
            listOf(
                "git",
                "-C",
                worktreePath,
                "rev-parse",
                "--abbrev-ref",
                "$branch@{upstream}",
            ),
        )
    val hasRemote = trackingResult.exitCode == 0
    var ahead = 0
    var behind = 0
    if (hasRemote) {
        val revListResult =
            macOsGitRunCommand(
                listOf(
                    "git",
                    "-C",
                    worktreePath,
                    "rev-list",
                    "--left-right",
                    "--count",
                    "$branch@{upstream}...$branch",
                ),
            )
        if (revListResult.exitCode == 0) {
            val parts = revListResult.output.trim().split(Regex("\\s+"))
            if (parts.size >= 2) {
                behind = parts[0].toIntOrNull() ?: 0
                ahead = parts[1].toIntOrNull() ?: 0
            }
        }
    }

    val prStatus = macOsGitGetPrStatus(worktreePath = worktreePath, branch = branch)
    return WorktreeStatus(
        isDirty = isDirty,
        hasRemote = hasRemote,
        ahead = ahead,
        behind = behind,
        prStatus = prStatus,
    )
}
