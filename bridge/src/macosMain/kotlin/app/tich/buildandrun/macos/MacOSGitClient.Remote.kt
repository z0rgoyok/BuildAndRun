package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.shared.error.GitError

internal suspend fun macOsGitPush(
    atWorktreePath: String,
    setUpstream: Boolean,
) {
    val worktreePath = macOsGitNormalizePath(atWorktreePath)
    val command =
        buildList {
            addAll(listOf("git", "-C", worktreePath, "push"))
            if (setUpstream) {
                addAll(listOf("-u", "origin", "HEAD"))
            }
        }
    val result = macOsGitRunCommand(command)
    if (result.exitCode != 0) {
        throw macOsGitMapCommandError(
            result = result,
            repoPath = worktreePath,
            branch = null,
            worktreePath = worktreePath,
        )
    }
}

internal suspend fun macOsGitPull(atWorktreePath: String) {
    val worktreePath = macOsGitNormalizePath(atWorktreePath)
    val result = macOsGitRunCommand(listOf("git", "-C", worktreePath, "pull"))
    if (result.exitCode != 0) {
        throw macOsGitMapCommandError(
            result = result,
            repoPath = worktreePath,
            branch = null,
            worktreePath = worktreePath,
        )
    }
}

internal suspend fun macOsGitCreatePr(
    atWorktreePath: String,
    title: String,
    body: String,
    baseBranch: String?,
): String {
    val worktreePath = macOsGitNormalizePath(atWorktreePath)
    val command =
        buildList {
            addAll(listOf("gh", "pr", "create", "--title", title, "--body", body))
            baseBranch?.trim()?.takeIf(String::isNotBlank)?.let {
                add("--base")
                add(it)
            }
        }
    val result =
        macOsGitRunCommand(
            arguments = command,
            workingDirectory = worktreePath,
        )
    if (result.exitCode != 0) {
        throw GitError.PRCreationFailed(
            reason =
                result.output.ifBlank {
                    "exit_code=${result.exitCode}"
                },
        )
    }
    return result.output.trim()
}

internal suspend fun macOsGitMergeBranch(
    atRepoPath: String,
    source: String,
    intoTarget: String,
) {
    val repoPath = macOsGitNormalizePath(atRepoPath)
    val sourceBranch = source.trim()
    val targetBranch = intoTarget.trim()
    val checkoutResult = macOsGitRunCommand(listOf("git", "-C", repoPath, "checkout", targetBranch))
    if (checkoutResult.exitCode != 0) {
        throw macOsGitMapCommandError(
            result = checkoutResult,
            repoPath = repoPath,
            branch = targetBranch,
            worktreePath = null,
        )
    }
    val mergeResult = macOsGitRunCommand(listOf("git", "-C", repoPath, "merge", sourceBranch, "--no-edit"))
    if (mergeResult.exitCode != 0) {
        val output = mergeResult.output.lowercase()
        if (output.contains("conflict") || output.contains("merge conflict")) {
            throw GitError.MergeConflict(source = sourceBranch, target = targetBranch)
        }
        throw macOsGitMapCommandError(
            result = mergeResult,
            repoPath = repoPath,
            branch = sourceBranch,
            worktreePath = null,
        )
    }
}

internal suspend fun macOsGitDeleteRemoteBranch(
    atRepoPath: String,
    branch: String,
) {
    val repoPath = macOsGitNormalizePath(atRepoPath)
    val branchName = branch.trim()
    val result = macOsGitRunCommand(listOf("git", "-C", repoPath, "push", "origin", "--delete", branchName))
    if (result.exitCode != 0) {
        throw macOsGitMapCommandError(
            result = result,
            repoPath = repoPath,
            branch = branchName,
            worktreePath = null,
        )
    }
}

internal suspend fun macOsGitHasRemoteBranch(
    atRepoPath: String,
    branch: String,
): Boolean {
    val repoPath = macOsGitNormalizePath(atRepoPath)
    val branchName = branch.trim()
    val result = macOsGitRunCommand(listOf("git", "-C", repoPath, "ls-remote", "--heads", "origin", branchName))
    return result.exitCode == 0 && result.output.trim().isNotEmpty()
}
