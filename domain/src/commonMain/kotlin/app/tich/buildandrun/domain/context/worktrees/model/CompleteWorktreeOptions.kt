package app.tich.buildandrun.domain.context.worktrees.model

data class CompleteWorktreeOptions(
    val targetBranch: String,
    val mergeIntoTarget: Boolean,
    val pullTargetFirst: Boolean,
    val deleteLocalBranch: Boolean,
    val deleteRemoteBranch: Boolean,
    val force: Boolean,
) {
    init {
        require(targetBranch.isNotBlank()) { "Target branch cannot be blank" }
    }

    companion object {
        fun simple(targetBranch: String = "main") =
            CompleteWorktreeOptions(
                targetBranch = targetBranch,
                mergeIntoTarget = false,
                pullTargetFirst = false,
                deleteLocalBranch = false,
                deleteRemoteBranch = false,
                force = false,
            )

        fun fullCleanup(targetBranch: String = "main") =
            CompleteWorktreeOptions(
                targetBranch = targetBranch,
                mergeIntoTarget = true,
                pullTargetFirst = true,
                deleteLocalBranch = true,
                deleteRemoteBranch = true,
                force = false,
            )
    }
}
