package app.tich.buildandrun.macos

import app.tich.buildandrun.application.context.shared.path.normalizePath
import app.tich.buildandrun.domain.context.kanban.model.PRState
import app.tich.buildandrun.domain.context.kanban.model.PRStatus
import app.tich.buildandrun.domain.context.worktrees.model.Worktree
import app.tich.buildandrun.domain.shared.error.GitError

internal data class MacOSGitCommandResult(
    val exitCode: Int,
    val output: String,
)

internal fun macOsGitNormalizePath(path: String): String = normalizePath(path)

internal fun macOsGitRunCommand(
    arguments: List<String>,
    workingDirectory: String? = null,
): MacOSGitCommandResult {
    val (exitCode, output) =
        runShellCommand(
            arguments = arguments,
            workingDirectory = workingDirectory,
        )
    return MacOSGitCommandResult(exitCode = exitCode, output = output)
}

internal fun macOsGitMapCommandError(
    result: MacOSGitCommandResult,
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

internal fun macOsGitParseWorktrees(
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

    val normalizedRepoPath = macOsGitNormalizePath(repoPath)
    return blocks.mapNotNull { block ->
        var path: String? = null
        var branch: String? = null
        var head: String? = null
        var isLocked = false
        var isPrunable = false
        var isDetachedHead = false

        block.forEach { line ->
            when {
                line.startsWith("worktree ") -> path = macOsGitNormalizePath(line.removePrefix("worktree ").trim())
                line.startsWith("branch ") ->
                    branch =
                        line.removePrefix("branch ")
                            .trim()
                            .removePrefix("refs/heads/")
                line.startsWith("HEAD ") -> head = line.removePrefix("HEAD ").trim()
                line.startsWith("locked") -> isLocked = true
                line.startsWith("prunable") -> isPrunable = true
                line == "detached" -> isDetachedHead = true
            }
        }

        val resolvedPath = path ?: return@mapNotNull null
        Worktree(
            path = resolvedPath,
            branch = branch?.ifBlank { "detached" } ?: "detached",
            isMain = macOsGitNormalizePath(resolvedPath) == normalizedRepoPath,
            commitHash = head?.ifBlank { null },
            isLocked = isLocked,
            isPrunable = isPrunable,
            baseBranch = null,
            isDetachedHead = isDetachedHead,
        )
    }
}

internal fun macOsGitGetPrStatus(
    worktreePath: String,
    branch: String,
): PRStatus? {
    val result =
        macOsGitRunCommand(
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
    val number =
        """"number"\s*:\s*(\d+)""".toRegex().find(json)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: return null
    val stateRaw =
        """"state"\s*:\s*"([^"]+)"""".toRegex().find(json)?.groupValues?.getOrNull(1) ?: return null
    val url =
        """"url"\s*:\s*"([^"]+)"""".toRegex().find(json)?.groupValues?.getOrNull(1)?.let(::macOsGitUnescapeJson) ?: return null
    val title =
        """"title"\s*:\s*"([^"]*)"""".toRegex().find(json)?.groupValues?.getOrNull(1)?.let(::macOsGitUnescapeJson)
    return PRStatus(
        number = number,
        state = PRState.fromString(stateRaw),
        url = url,
        title = title?.ifBlank { null },
    )
}

internal fun macOsGitUnescapeJson(value: String): String = value.replace("\\/", "/").replace("\\\"", "\"")
