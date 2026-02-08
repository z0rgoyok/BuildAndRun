package app.tich.buildandrun.appstore

import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.resources.*

data class KanbanLabels(
    val delete: String,
    val moveTo: String,
    val selectWorktreeHint: String,
    val badgeMain: String,
    val badgeLocked: String,
    val copyPath: String,
    val createWorktreeTitle: String,
    val createWorktreeName: String,
    val createBranchNew: String,
    val createBranchExisting: String,
    val createBranchName: String,
    val createBaseBranch: String,
    val createLoadingBranches: String,
    val createLocation: String,
    val createButton: String,
    val createCancel: String,
    val createPreparing: String,
    val createSubmitting: String,
    val createBranchPicker: String,
    val branchConflictTitle: String,
    val branchConflictUseExisting: String,
    val branchConflictRecreate: String,
    val branchConflictRecreateDetail: String,
    val toolbarNewWorktree: String,
    val toolbarFinder: String,
    val toolbarTerminal: String,
    val toolbarRefresh: String,
)

internal fun buildKanbanLabels(): KanbanLabels =
    KanbanLabels(
        delete = resolveText(text = UiText(resource = Res.string.action_delete)),
        moveTo = resolveText(text = UiText(resource = Res.string.action_move_to)),
        selectWorktreeHint = resolveText(text = UiText(resource = Res.string.detail_select_worktree_hint)),
        badgeMain = resolveText(text = UiText(resource = Res.string.detail_badge_main)),
        badgeLocked = resolveText(text = UiText(resource = Res.string.detail_badge_locked)),
        copyPath = resolveText(text = UiText(resource = Res.string.action_copy_path)),
        createWorktreeTitle = resolveText(text = UiText(resource = Res.string.worktree_create_title)),
        createWorktreeName = resolveText(text = UiText(resource = Res.string.worktree_create_name)),
        createBranchNew = resolveText(text = UiText(resource = Res.string.worktree_create_branch_new)),
        createBranchExisting = resolveText(text = UiText(resource = Res.string.worktree_create_branch_existing)),
        createBranchName = resolveText(text = UiText(resource = Res.string.worktree_create_branch_name)),
        createBaseBranch = resolveText(text = UiText(resource = Res.string.worktree_create_base_branch)),
        createLoadingBranches = resolveText(text = UiText(resource = Res.string.worktree_create_loading_branches)),
        createLocation = resolveText(text = UiText(resource = Res.string.worktree_create_location)),
        createButton = resolveText(text = UiText(resource = Res.string.worktree_create_button)),
        createCancel = resolveText(text = UiText(resource = Res.string.worktree_create_cancel)),
        createPreparing = resolveText(text = UiText(resource = Res.string.worktree_create_preparing)),
        createSubmitting = resolveText(text = UiText(resource = Res.string.worktree_create_submitting)),
        createBranchPicker = resolveText(text = UiText(resource = Res.string.worktree_create_branch_picker)),
        branchConflictTitle = resolveText(text = UiText(resource = Res.string.branch_conflict_title)),
        branchConflictUseExisting = resolveText(text = UiText(resource = Res.string.branch_conflict_use_existing)),
        branchConflictRecreate = resolveText(text = UiText(resource = Res.string.branch_conflict_recreate)),
        branchConflictRecreateDetail = resolveText(text = UiText(resource = Res.string.branch_conflict_recreate_detail)),
        toolbarNewWorktree = resolveText(text = UiText(resource = Res.string.toolbar_new_worktree)),
        toolbarFinder = resolveText(text = UiText(resource = Res.string.toolbar_finder)),
        toolbarTerminal = resolveText(text = UiText(resource = Res.string.toolbar_terminal)),
        toolbarRefresh = resolveText(text = UiText(resource = Res.string.toolbar_refresh)),
    )

internal fun resolveBranchConflictMessageText(branch: String): String =
    resolveText(text = UiText(resource = Res.string.branch_conflict_message, args = listOf(branch)))

internal fun resolveBranchConflictUseExistingDetailText(branch: String): String =
    resolveText(text = UiText(resource = Res.string.branch_conflict_use_existing_detail, args = listOf(branch)))
