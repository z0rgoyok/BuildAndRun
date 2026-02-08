@file:Suppress("unused")

package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.entities.KanbanColumnType
import app.tich.buildandrun.domain.entities.WorktreeStatus
import app.tich.buildandrun.domain.failures.DomainFailureCode
import com.arkivanov.decompose.value.Value

class MacOSAppStore {
    private val core = MacOSAppStoreCore()

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
        val isMain: Boolean,
        val isLocked: Boolean,
        val isPrunable: Boolean,
        val status: WorktreeStatus?,
        val isStatusLoading: Boolean,
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
