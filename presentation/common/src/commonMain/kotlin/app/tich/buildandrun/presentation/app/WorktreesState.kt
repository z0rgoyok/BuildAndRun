package app.tich.buildandrun.presentation.app

data class WorktreesState(
    val selectedWorktreePath: String? = null,
    val remoteBranches: List<RemoteBranchItem> = emptyList(),
    val createWorktree: CreateWorktreeState = CreateWorktreeState(),
)
