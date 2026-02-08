@file:Suppress("unused")

package app.tich.buildandrun.appstore

import app.tich.buildandrun.domain.entities.KanbanColumnType
import app.tich.buildandrun.domain.entities.WorktreeStatus
import app.tich.buildandrun.domain.failures.DomainFailureCode
import com.arkivanov.decompose.value.Value

class AppStore internal constructor(
    graph: AppStoreGraph,
) {
    private val core = AppStoreCore(graph = graph)

    val state: Value<State> = core.state

    fun onAddRepositoryPathChanged(value: String) {
        core.onAddRepositoryPathChanged(value = value)
    }

    fun onAddRepository() {
        core.onAddRepository()
    }

    fun onSelectRepository(repositoryId: String) {
        core.onSelectRepository(repositoryId = repositoryId)
    }

    fun onArchiveRepository(repositoryId: String) {
        core.onArchiveRepository(repositoryId = repositoryId)
    }

    fun onRestoreRepository(repositoryId: String) {
        core.onRestoreRepository(repositoryId = repositoryId)
    }

    fun onRemoveRepository(repositoryId: String) {
        core.onRemoveRepository(repositoryId = repositoryId)
    }

    fun onSelectWorktree(worktreePath: String?) {
        core.onSelectWorktree(worktreePath = worktreePath)
    }

    fun onRefreshSelectedRepository() {
        core.onRefreshSelectedRepository()
    }

    fun onRefreshWorktreeStatus(worktreePath: String) {
        core.onRefreshWorktreeStatus(worktreePath = worktreePath)
    }

    fun onLoadBranches() {
        core.onLoadBranches()
    }

    fun branchExists(branch: String): Boolean = core.branchExists(branch = branch)

    fun onSetWorktreeBasePath(path: String) {
        core.onSetWorktreeBasePath(path = path)
    }

    fun preferredBaseBranch(): String? = core.preferredBaseBranch()

    fun onSetPreferredBaseBranch(branch: String) {
        core.onSetPreferredBaseBranch(branch = branch)
    }

    fun onSetDefaultCopyPatterns(patterns: List<String>) {
        core.onSetDefaultCopyPatterns(patterns = patterns)
    }

    fun onSetRepositoryCopyPatterns(patterns: List<String>?) {
        core.onSetRepositoryCopyPatterns(patterns = patterns)
    }

    fun onPush(worktreePath: String) {
        core.onPush(worktreePath = worktreePath)
    }

    fun onPull(worktreePath: String) {
        core.onPull(worktreePath = worktreePath)
    }

    fun onCreatePullRequest(
        worktreePath: String,
        title: String,
        body: String,
        baseBranch: String?,
    ) {
        core.onCreatePullRequest(
            worktreePath = worktreePath,
            title = title,
            body = body,
            baseBranch = baseBranch,
        )
    }

    fun onOpenPullRequest(worktreePath: String) {
        core.onOpenPullRequest(worktreePath = worktreePath)
    }

    fun onLockWorktree(worktreePath: String) {
        core.onLockWorktree(worktreePath = worktreePath)
    }

    fun onUnlockWorktree(worktreePath: String) {
        core.onUnlockWorktree(worktreePath = worktreePath)
    }

    fun onRemoveWorktree(
        worktreePath: String,
        force: Boolean,
        deleteBranch: Boolean,
    ) {
        core.onRemoveWorktree(
            worktreePath = worktreePath,
            force = force,
            deleteBranch = deleteBranch,
        )
    }

    fun onCompleteWorktree(
        worktreePath: String,
        targetBranch: String,
        mergeIntoTarget: Boolean,
        pullTargetFirst: Boolean,
        deleteLocalBranch: Boolean,
        deleteRemoteBranch: Boolean,
        force: Boolean,
    ) {
        core.onCompleteWorktree(
            worktreePath = worktreePath,
            targetBranch = targetBranch,
            mergeIntoTarget = mergeIntoTarget,
            pullTargetFirst = pullTargetFirst,
            deleteLocalBranch = deleteLocalBranch,
            deleteRemoteBranch = deleteRemoteBranch,
            force = force,
        )
    }

    fun onLoadHasRemoteBranch(worktreePath: String) {
        core.onLoadHasRemoteBranch(worktreePath = worktreePath)
    }

    fun onPruneWorktrees() {
        core.onPruneWorktrees()
    }

    fun onSetRememberEditorChoice(value: Boolean) {
        core.onSetRememberEditorChoice(value = value)
    }

    fun onSetEditorEnabled(
        editorId: String,
        enabled: Boolean,
    ) {
        core.onSetEditorEnabled(
            editorId = editorId,
            enabled = enabled,
        )
    }

    fun onSetPreferredEditor(editorId: String?) {
        core.onSetPreferredEditor(editorId = editorId)
    }

    fun onOpenInEditor(
        worktreePath: String,
        editorId: String?,
    ) {
        core.onOpenInEditor(
            worktreePath = worktreePath,
            editorId = editorId,
        )
    }

    fun onOpenInFinder(worktreePath: String) {
        core.onOpenInFinder(worktreePath = worktreePath)
    }

    fun onOpenInTerminal(worktreePath: String) {
        core.onOpenInTerminal(worktreePath = worktreePath)
    }

    fun onCreateWorktreeBranchChanged(value: String) {
        core.onCreateWorktreeBranchChanged(value = value)
    }

    fun onCreateWorktreePathChanged(value: String) {
        core.onCreateWorktreePathChanged(value = value)
    }

    fun onCreateWorktreeBaseBranchChanged(value: String) {
        core.onCreateWorktreeBaseBranchChanged(value = value)
    }

    fun onCreateWorktreeCreateBranchChanged(value: Boolean) {
        core.onCreateWorktreeCreateBranchChanged(value = value)
    }

    fun onCreateWorktree() {
        core.onCreateWorktree()
    }

    fun onAddTask(
        title: String,
        description: String?,
        column: KanbanColumnType,
    ) {
        core.onAddTask(title = title, description = description, column = column)
    }

    fun onMoveTask(
        taskId: String,
        column: KanbanColumnType,
    ) {
        core.onMoveTask(taskId = taskId, column = column)
    }

    fun onDeleteTask(taskId: String) {
        core.onDeleteTask(taskId = taskId)
    }

    fun onDismissError() {
        core.onDismissError()
    }

    fun onDismissSuccess() {
        core.onDismissSuccess()
    }

    fun destroy() {
        core.destroy()
    }

    data class State(
        val isLoading: Boolean = false,
        val repositories: List<RepositoryItem> = emptyList(),
        val selectedRepositoryId: String? = null,
        val selectedWorktreePath: String? = null,
        val addRepositoryPathInput: String = "",
        val branches: List<String> = emptyList(),
        val worktreeBasePath: String = "",
        val defaultCopyPatterns: List<String> = emptyList(),
        val selectedRepositoryCustomCopyPatterns: List<String>? = null,
        val selectedRepositoryEffectiveCopyPatterns: List<String> = emptyList(),
        val rememberEditorChoice: Boolean = true,
        val preferredEditorId: String? = null,
        val editors: List<EditorItem> = emptyList(),
        val remoteBranches: List<RemoteBranchItem> = emptyList(),
        val createWorktree: CreateWorktreeState = CreateWorktreeState(),
        val kanbanTasks: List<KanbanTaskItem> = emptyList(),
        val error: ErrorState? = null,
        val success: SuccessState? = null,
    )

    data class RepositoryItem(
        val id: String,
        val name: String,
        val path: String,
        val isArchived: Boolean,
        val worktrees: List<WorktreeItem>,
    )

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

    data class EditorItem(
        val id: String,
        val name: String,
        val icon: String,
        val isInstalled: Boolean,
        val isEnabled: Boolean,
    )

    data class RemoteBranchItem(
        val worktreePath: String,
        val hasRemote: Boolean,
    )

    data class CreateWorktreeState(
        val repositoryPath: String = "",
        val branchInput: String = "",
        val worktreePathInput: String = "",
        val baseBranchInput: String = "",
        val createBranch: Boolean = true,
        val isSubmitting: Boolean = false,
        val createdWorktreePath: String? = null,
    )

    data class KanbanTaskItem(
        val id: String,
        val title: String,
        val description: String?,
        val columnId: KanbanColumnType,
        val order: Int,
    )

    data class ErrorState(
        val code: DomainFailureCode,
        val message: String,
        val details: String?,
        val isRetryable: Boolean,
    )

    data class SuccessState(
        val message: String,
    )
}
