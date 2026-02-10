package app.tich.buildandrun.presentation.app.context.state

import app.tich.buildandrun.domain.context.worktrees.model.Worktree
import app.tich.buildandrun.domain.context.worktrees.model.WorktreeStatus
import app.tich.buildandrun.presentation.app.CreateWorktreeState

class WorktreesContextState {
    val worktreesByRepositoryPath = mutableMapOf<String, List<Worktree>>()
    val worktreeStatusByPath = mutableMapOf<String, WorktreeStatus>()
    val worktreeStatusLoadingPaths = mutableSetOf<String>()
    val hasRemoteBranchByWorktreePath = mutableMapOf<String, Boolean>()
    var createWorktreeState: CreateWorktreeState = CreateWorktreeState()
    var selectedWorktreePath: String? = null
}
