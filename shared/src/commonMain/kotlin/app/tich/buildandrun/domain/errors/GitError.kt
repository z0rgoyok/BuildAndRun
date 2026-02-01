package app.tich.buildandrun.domain.errors

sealed class GitError(
    override val message: String,
) : Exception(message) {
    data class NotARepository(val path: String) : GitError(
        "Path is not inside a git repository: $path",
    )

    data class WorktreeAlreadyExists(val name: String) : GitError(
        "Worktree already exists: $name",
    )

    data class WorktreeNotFound(val path: String) : GitError(
        "Worktree not found: $path",
    )

    data class BranchAlreadyExists(val name: String) : GitError(
        "Branch already exists: $name",
    )

    data class BranchNotFound(val name: String) : GitError(
        "Branch not found: $name",
    )

    data object CannotRemoveMainWorktree : GitError(
        "Cannot remove the main worktree",
    )

    data class CommandFailed(val errorMessage: String) : GitError(
        "Git command failed: $errorMessage",
    )

    data class InvalidPath(val path: String) : GitError(
        "Invalid path: $path",
    )

    data class WorktreeHasUncommittedChanges(val path: String) : GitError(
        "Worktree has uncommitted changes: $path",
    )

    data class PRCreationFailed(val reason: String) : GitError(
        "Failed to create pull request: $reason",
    )

    data class MergeConflict(val source: String, val target: String) : GitError(
        "Merge conflict: cannot merge '$source' into '$target'",
    )
}
