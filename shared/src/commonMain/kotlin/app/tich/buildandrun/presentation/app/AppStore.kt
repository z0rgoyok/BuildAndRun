@file:Suppress("unused")

package app.tich.buildandrun.presentation.app

import app.tich.buildandrun.domain.context.kanban.model.KanbanColumnType
import app.tich.buildandrun.domain.context.worktrees.model.WorktreeStatus
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

class AppStore private constructor() {
    data class State(
        val isLoading: Boolean = false,
        val loadingMessage: String? = null,
        val repositories: List<RepositoryItem> = emptyList(),
        val sidebarSections: List<SidebarSection> = emptyList(),
        val expandedRepositoryIds: Set<String> = emptySet(),
        val collapsedGroupIds: Set<String> = emptySet(),
        val repositoryGroups: List<RepositoryGroupItem> = emptyList(),
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
        val activeChild: AppChild = AppChild.WORKSPACE,
        val activeSheet: AppSheetState? = null,
        val error: ErrorState? = null,
        val success: SuccessState? = null,
    )

    data class RepositoryItem(
        val id: String,
        val name: String,
        val path: String,
        val isArchived: Boolean,
        val groupId: String? = null,
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

    data class SidebarSection(
        val groupId: String?,
        val groupName: String?,
        val repositories: List<RepositoryItem>,
    )

    data class RepositoryGroupItem(
        val id: String,
        val name: String,
    )
}
