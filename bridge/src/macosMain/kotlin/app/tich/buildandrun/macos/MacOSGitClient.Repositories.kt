package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.shared.error.GitError

internal suspend fun macOsGitGetRepositoryRoot(atPath: String): String {
    val normalizedPath = macOsGitNormalizePath(atPath)
    if (normalizedPath.isBlank()) {
        throw GitError.InvalidPath(path = atPath)
    }
    val result = macOsGitRunCommand(listOf("git", "-C", normalizedPath, "rev-parse", "--show-toplevel"))
    if (result.exitCode != 0) {
        throw macOsGitMapCommandError(
            result = result,
            repoPath = normalizedPath,
            branch = null,
            worktreePath = null,
        )
    }
    val root = macOsGitNormalizePath(result.output.lines().firstOrNull().orEmpty())
    if (root.isBlank()) {
        throw GitError.NotARepository(path = normalizedPath)
    }
    return root
}

internal suspend fun macOsGitListBranches(atRepoPath: String): List<String> {
    val repoPath = macOsGitNormalizePath(atRepoPath)
    val result = macOsGitRunCommand(listOf("git", "-C", repoPath, "branch", "--format=%(refname:short)"))
    if (result.exitCode != 0) {
        throw macOsGitMapCommandError(
            result = result,
            repoPath = repoPath,
            branch = null,
            worktreePath = null,
        )
    }
    return result.output.lines().map(String::trim).filter(String::isNotBlank)
}

internal suspend fun macOsGitBranchExists(
    atRepoPath: String,
    branch: String,
): Boolean {
    val repoPath = macOsGitNormalizePath(atRepoPath)
    val branchName = branch.trim()
    if (branchName.isBlank()) {
        throw GitError.BranchNotFound(name = branch)
    }
    val result =
        macOsGitRunCommand(
            listOf(
                "git",
                "-C",
                repoPath,
                "show-ref",
                "--verify",
                "--quiet",
                "refs/heads/$branchName",
            ),
        )
    return result.exitCode == 0
}

internal suspend fun macOsGitDeleteBranch(
    atRepoPath: String,
    branch: String,
    force: Boolean,
) {
    val repoPath = macOsGitNormalizePath(atRepoPath)
    val branchName = branch.trim()
    if (repoPath.isBlank()) {
        throw GitError.InvalidPath(path = atRepoPath)
    }
    if (branchName.isBlank()) {
        throw GitError.BranchNotFound(name = branch)
    }
    val flag = if (force) "-D" else "-d"
    val result = macOsGitRunCommand(listOf("git", "-C", repoPath, "branch", flag, branchName))
    if (result.exitCode != 0) {
        throw macOsGitMapCommandError(
            result = result,
            repoPath = repoPath,
            branch = branchName,
            worktreePath = null,
        )
    }
}
