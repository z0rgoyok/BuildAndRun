package app.tich.buildandrun.presentation.app

data class CreateWorktreeState(
    val repositoryPath: String = "",
    val branchInput: String = "",
    val worktreePathInput: String = "",
    val baseBranchInput: String = "",
    val createBranch: Boolean = true,
    val isSubmitting: Boolean = false,
    val createdWorktreePath: String? = null,
)
