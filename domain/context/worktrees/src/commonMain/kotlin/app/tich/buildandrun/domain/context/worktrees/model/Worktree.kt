package app.tich.buildandrun.domain.context.worktrees.model

data class Worktree(
    val path: String,
    val branch: String,
    val isMain: Boolean,
    val commitHash: String?,
    val isLocked: Boolean,
    val isPrunable: Boolean,
    val baseBranch: String?,
    val isDetachedHead: Boolean = false,
) {
    val id: String get() = path
    val name: String get() = path.substringAfterLast('/')

    fun withBaseBranch(baseBranch: String?): Worktree = copy(baseBranch = baseBranch)

    init {
        require(path.isNotBlank()) { "Worktree path cannot be blank" }
        if (!isDetachedHead) {
            require(branch.isNotBlank()) { "Worktree branch cannot be blank" }
        }
    }
}
