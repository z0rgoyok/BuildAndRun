package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.entities.PRState
import app.tich.buildandrun.domain.entities.PRStatus
import app.tich.buildandrun.domain.entities.Worktree
import app.tich.buildandrun.domain.entities.WorktreeStatus
import app.tich.buildandrun.domain.errors.GitError
import app.tich.buildandrun.domain.ports.GitClient

class MacOSGitClient : GitClient {
    override suspend fun getRepositoryRoot(atPath: String): String {
        val normalizedPath = normalizePath(atPath)
        if (normalizedPath.isBlank()) {
            throw GitError.InvalidPath(path = atPath)
        }
        val result = runCommand(listOf("git", "-C", normalizedPath, "rev-parse", "--show-toplevel"))
        if (result.exitCode != 0) {
            throw mapCommandError(
                result = result,
                repoPath = normalizedPath,
                branch = null,
                worktreePath = null,
            )
        }
        val root = normalizePath(result.output.lines().firstOrNull().orEmpty())
        if (root.isBlank()) {
            throw GitError.NotARepository(path = normalizedPath)
        }
        return root
    }

    override suspend fun listBranches(atRepoPath: String): List<String> {
        val repoPath = normalizePath(atRepoPath)
        val result = runCommand(listOf("git", "-C", repoPath, "branch", "--format=%(refname:short)"))
        if (result.exitCode != 0) {
            throw mapCommandError(result = result, repoPath = repoPath, branch = null, worktreePath = null)
        }
        return result.output.lines().map { it.trim() }.filter { it.isNotBlank() }
    }

    override suspend fun branchExists(
        atRepoPath: String,
        branch: String,
    ): Boolean {
        val repoPath = normalizePath(atRepoPath)
        val branchName = branch.trim()
        if (branchName.isBlank()) {
            throw GitError.BranchNotFound(name = branch)
        }
        val result = runCommand(listOf("git", "-C", repoPath, "show-ref", "--verify", "--quiet", "refs/heads/$branchName"))
        return result.exitCode == 0
    }

    override suspend fun deleteBranch(
        atRepoPath: String,
        branch: String,
        force: Boolean,
    ) {
        val repoPath = normalizePath(atRepoPath)
        val branchName = branch.trim()
        if (repoPath.isBlank()) {
            throw GitError.InvalidPath(path = atRepoPath)
        }
        if (branchName.isBlank()) {
            throw GitError.BranchNotFound(name = branch)
        }
        val flag = if (force) "-D" else "-d"
        val result = runCommand(listOf("git", "-C", repoPath, "branch", flag, branchName))
        if (result.exitCode != 0) {
            throw mapCommandError(result = result, repoPath = repoPath, branch = branchName, worktreePath = null)
        }
    }

    override suspend fun listWorktrees(atRepoPath: String): List<Worktree> {
        val repoPath = normalizePath(atRepoPath)
        val result = runCommand(listOf("git", "-C", repoPath, "worktree", "list", "--porcelain"))
        if (result.exitCode != 0) {
            throw mapCommandError(result = result, repoPath = repoPath, branch = null, worktreePath = null)
        }
        return parseWorktrees(
            repoPath = repoPath,
            output = result.output,
        )
    }

    override suspend fun createWorktree(
        atRepoPath: String,
        worktreePath: String,
        branch: String,
        createBranch: Boolean,
        baseBranch: String?,
    ) {
        val repoPath = normalizePath(atRepoPath)
        val branchName = branch.trim()
        val targetWorktreePath = normalizePath(worktreePath)
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
                    baseBranch?.trim()?.takeIf { it.isNotBlank() }?.let(::add)
                } else {
                    add(targetWorktreePath)
                    add(branchName)
                }
            }
        val result = runCommand(command)
        if (result.exitCode != 0) {
            throw mapCommandError(
                result = result,
                repoPath = repoPath,
                branch = branchName,
                worktreePath = targetWorktreePath,
            )
        }
    }

    override suspend fun removeWorktree(
        atRepoPath: String,
        worktreePath: String,
        force: Boolean,
    ) {
        val repoPath = normalizePath(atRepoPath)
        val targetWorktreePath = normalizePath(worktreePath)
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
        val result = runCommand(command)
        if (result.exitCode != 0) {
            throw mapCommandError(
                result = result,
                repoPath = repoPath,
                branch = null,
                worktreePath = targetWorktreePath,
            )
        }
    }

    override suspend fun lockWorktree(
        atRepoPath: String,
        worktreePath: String,
        reason: String?,
    ) {
        val repoPath = normalizePath(atRepoPath)
        val targetWorktreePath = normalizePath(worktreePath)
        val command =
            buildList {
                addAll(listOf("git", "-C", repoPath, "worktree", "lock"))
                reason?.trim()?.takeIf { it.isNotBlank() }?.let {
                    add("--reason")
                    add(it)
                }
                add(targetWorktreePath)
            }
        val result = runCommand(command)
        if (result.exitCode != 0) {
            throw mapCommandError(
                result = result,
                repoPath = repoPath,
                branch = null,
                worktreePath = targetWorktreePath,
            )
        }
    }

    override suspend fun unlockWorktree(
        atRepoPath: String,
        worktreePath: String,
    ) {
        val repoPath = normalizePath(atRepoPath)
        val targetWorktreePath = normalizePath(worktreePath)
        val result =
            runCommand(
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
            throw mapCommandError(
                result = result,
                repoPath = repoPath,
                branch = null,
                worktreePath = targetWorktreePath,
            )
        }
    }

    override suspend fun pruneWorktrees(atRepoPath: String) {
        val repoPath = normalizePath(atRepoPath)
        val result = runCommand(listOf("git", "-C", repoPath, "worktree", "prune"))
        if (result.exitCode != 0) {
            throw mapCommandError(result = result, repoPath = repoPath, branch = null, worktreePath = null)
        }
    }

    override suspend fun getWorktreeStatus(atWorktreePath: String): WorktreeStatus {
        val worktreePath = normalizePath(atWorktreePath)
        if (worktreePath.isBlank()) {
            throw GitError.InvalidPath(path = atWorktreePath)
        }

        val statusResult = runCommand(listOf("git", "-C", worktreePath, "status", "--porcelain"))
        if (statusResult.exitCode != 0) {
            throw mapCommandError(
                result = statusResult,
                repoPath = worktreePath,
                branch = null,
                worktreePath = worktreePath,
            )
        }
        val isDirty = statusResult.output.isNotBlank()

        val branchResult = runCommand(listOf("git", "-C", worktreePath, "branch", "--show-current"))
        if (branchResult.exitCode != 0) {
            throw mapCommandError(
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
            runCommand(
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
                runCommand(
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

        val prStatus = getPRStatus(worktreePath = worktreePath, branch = branch)
        return WorktreeStatus(
            isDirty = isDirty,
            hasRemote = hasRemote,
            ahead = ahead,
            behind = behind,
            prStatus = prStatus,
        )
    }

    override suspend fun push(
        atWorktreePath: String,
        setUpstream: Boolean,
    ) {
        val worktreePath = normalizePath(atWorktreePath)
        val command =
            buildList {
                addAll(listOf("git", "-C", worktreePath, "push"))
                if (setUpstream) {
                    addAll(listOf("-u", "origin", "HEAD"))
                }
            }
        val result = runCommand(command)
        if (result.exitCode != 0) {
            throw mapCommandError(
                result = result,
                repoPath = worktreePath,
                branch = null,
                worktreePath = worktreePath,
            )
        }
    }

    override suspend fun pull(atWorktreePath: String) {
        val worktreePath = normalizePath(atWorktreePath)
        val result = runCommand(listOf("git", "-C", worktreePath, "pull"))
        if (result.exitCode != 0) {
            throw mapCommandError(
                result = result,
                repoPath = worktreePath,
                branch = null,
                worktreePath = worktreePath,
            )
        }
    }

    override suspend fun createPR(
        atWorktreePath: String,
        title: String,
        body: String,
        baseBranch: String?,
    ): String {
        val worktreePath = normalizePath(atWorktreePath)
        val command =
            buildList {
                addAll(listOf("gh", "pr", "create", "--title", title, "--body", body))
                baseBranch?.trim()?.takeIf { it.isNotBlank() }?.let {
                    add("--base")
                    add(it)
                }
            }
        val result = runCommand(arguments = command, workingDirectory = worktreePath)
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

    override suspend fun mergeBranch(
        atRepoPath: String,
        source: String,
        intoTarget: String,
    ) {
        val repoPath = normalizePath(atRepoPath)
        val sourceBranch = source.trim()
        val targetBranch = intoTarget.trim()

        val checkoutResult = runCommand(listOf("git", "-C", repoPath, "checkout", targetBranch))
        if (checkoutResult.exitCode != 0) {
            throw mapCommandError(
                result = checkoutResult,
                repoPath = repoPath,
                branch = targetBranch,
                worktreePath = null,
            )
        }

        val mergeResult = runCommand(listOf("git", "-C", repoPath, "merge", sourceBranch, "--no-edit"))
        if (mergeResult.exitCode != 0) {
            val output = mergeResult.output.lowercase()
            if (output.contains("conflict") || output.contains("merge conflict")) {
                throw GitError.MergeConflict(source = sourceBranch, target = targetBranch)
            }
            throw mapCommandError(
                result = mergeResult,
                repoPath = repoPath,
                branch = sourceBranch,
                worktreePath = null,
            )
        }
    }

    override suspend fun deleteRemoteBranch(
        atRepoPath: String,
        branch: String,
    ) {
        val repoPath = normalizePath(atRepoPath)
        val branchName = branch.trim()
        val result = runCommand(listOf("git", "-C", repoPath, "push", "origin", "--delete", branchName))
        if (result.exitCode != 0) {
            throw mapCommandError(result = result, repoPath = repoPath, branch = branchName, worktreePath = null)
        }
    }

    override suspend fun hasRemoteBranch(
        atRepoPath: String,
        branch: String,
    ): Boolean {
        val repoPath = normalizePath(atRepoPath)
        val branchName = branch.trim()
        val result = runCommand(listOf("git", "-C", repoPath, "ls-remote", "--heads", "origin", branchName))
        return result.exitCode == 0 && result.output.trim().isNotEmpty()
    }

    private fun parseWorktrees(
        repoPath: String,
        output: String,
    ): List<Worktree> {
        val blocks = mutableListOf<List<String>>()
        var currentBlock = mutableListOf<String>()
        output.lines().forEach { line ->
            if (line.isBlank()) {
                if (currentBlock.isNotEmpty()) {
                    blocks += currentBlock
                    currentBlock = mutableListOf()
                }
            } else {
                currentBlock += line
            }
        }
        if (currentBlock.isNotEmpty()) {
            blocks += currentBlock
        }

        val normalizedRepoPath = normalizePath(repoPath)
        return blocks.mapNotNull { block ->
            var path: String? = null
            var branch: String? = null
            var head: String? = null
            var isLocked = false
            var isPrunable = false

            block.forEach { line ->
                when {
                    line.startsWith("worktree ") -> path = normalizePath(line.removePrefix("worktree ").trim())
                    line.startsWith("branch ") ->
                        branch =
                            line.removePrefix("branch ")
                                .trim()
                                .removePrefix("refs/heads/")
                    line.startsWith("HEAD ") -> head = line.removePrefix("HEAD ").trim()
                    line.startsWith("locked") -> isLocked = true
                    line.startsWith("prunable") -> isPrunable = true
                    line == "detached" -> branch = "detached"
                }
            }

            val resolvedPath = path ?: return@mapNotNull null
            Worktree(
                path = resolvedPath,
                branch = branch?.ifBlank { "detached" } ?: "detached",
                isMain = normalizePath(resolvedPath) == normalizedRepoPath,
                commitHash = head?.ifBlank { null },
                isLocked = isLocked,
                isPrunable = isPrunable,
                baseBranch = null,
            )
        }
    }

    private fun mapCommandError(
        result: CommandResult,
        repoPath: String,
        branch: String?,
        worktreePath: String?,
    ): GitError {
        val outputLowerCase = result.output.lowercase()
        if (outputLowerCase.contains("not a git repository")) {
            return GitError.NotARepository(path = repoPath)
        }
        if (outputLowerCase.contains("no such file or directory")) {
            return GitError.InvalidPath(path = repoPath)
        }
        if (outputLowerCase.contains("cannot remove the main working tree")) {
            return GitError.CannotRemoveMainWorktree
        }
        if (outputLowerCase.contains("contains modified or untracked files")) {
            return GitError.WorktreeHasUncommittedChanges(path = worktreePath ?: repoPath)
        }
        if (outputLowerCase.contains("worktree") && outputLowerCase.contains("already exists")) {
            return GitError.WorktreeAlreadyExists(name = worktreePath?.substringAfterLast('/') ?: "worktree")
        }
        if (outputLowerCase.contains("branch") && outputLowerCase.contains("already exists")) {
            return GitError.BranchAlreadyExists(name = branch ?: "branch")
        }
        if (
            outputLowerCase.contains("unknown revision") ||
            outputLowerCase.contains("invalid reference") ||
            outputLowerCase.contains("not a valid object name")
        ) {
            return GitError.BranchNotFound(name = branch ?: "branch")
        }
        if (outputLowerCase.contains("conflict") || outputLowerCase.contains("merge conflict")) {
            return GitError.MergeConflict(source = branch ?: "source", target = "target")
        }
        return GitError.CommandFailed(
            errorMessage =
                result.output.ifBlank {
                    "exit_code=${result.exitCode}"
                },
        )
    }

    private fun getPRStatus(
        worktreePath: String,
        branch: String,
    ): PRStatus? {
        val result =
            runCommand(
                arguments =
                    listOf(
                        "gh",
                        "pr",
                        "view",
                        branch,
                        "--json",
                        "number,state,url,title",
                        "-R",
                        ".",
                    ),
                workingDirectory = worktreePath,
            )
        if (result.exitCode != 0) {
            return null
        }
        val json = result.output
        val number = """"number"\s*:\s*(\d+)""".toRegex().find(json)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: return null
        val stateRaw = """"state"\s*:\s*"([^"]+)"""".toRegex().find(json)?.groupValues?.getOrNull(1) ?: return null
        val url = """"url"\s*:\s*"([^"]+)"""".toRegex().find(json)?.groupValues?.getOrNull(1)?.let(::unescapeJson) ?: return null
        val title = """"title"\s*:\s*"([^"]*)"""".toRegex().find(json)?.groupValues?.getOrNull(1)?.let(::unescapeJson)
        return PRStatus(
            number = number,
            state = PRState.fromString(stateRaw),
            url = url,
            title = title?.ifBlank { null },
        )
    }

    private fun unescapeJson(value: String): String = value.replace("\\/", "/").replace("\\\"", "\"")

    private fun runCommand(
        arguments: List<String>,
        workingDirectory: String? = null,
    ): CommandResult {
        val (exitCode, output) =
            runShellCommand(
                arguments = arguments,
                workingDirectory = workingDirectory,
            )
        return CommandResult(exitCode = exitCode, output = output)
    }

    private fun normalizePath(path: String): String = path.trim().trimEnd('/')

    private data class CommandResult(
        val exitCode: Int,
        val output: String,
    )
}
