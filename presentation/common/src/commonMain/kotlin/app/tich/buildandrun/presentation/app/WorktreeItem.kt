package app.tich.buildandrun.presentation.app

import app.tich.buildandrun.domain.context.worktrees.model.WorktreeStatus

data class WorktreeItem(
    val path: String,
    val name: String,
    val branch: String,
    val baseBranch: String?,
    val isMain: Boolean,
    val isLocked: Boolean,
    val isPrunable: Boolean,
    val status: WorktreeStatus?,
    val isStatusLoading: Boolean,
)
