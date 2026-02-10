package app.tich.buildandrun.presentation.app.context.state

import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.context.worktrees.model.Worktree
import app.tich.buildandrun.domain.context.worktrees.model.WorktreeStatus
import app.tich.buildandrun.presentation.app.CreateWorktreeState
import app.tich.buildandrun.presentation.app.RemoteBranchItem
import app.tich.buildandrun.presentation.app.WorktreesState
import app.tich.buildandrun.presentation.app.core.normalizePath
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

class WorktreesContextState {
    private val mutableState = MutableValue(WorktreesState())

    val worktreesByRepositoryPath = mutableMapOf<String, List<Worktree>>()
    val worktreeStatusByPath = mutableMapOf<String, WorktreeStatus>()
    val worktreeStatusLoadingPaths = mutableSetOf<String>()
    val hasRemoteBranchByWorktreePath = mutableMapOf<String, Boolean>()
    var createWorktreeState: CreateWorktreeState = CreateWorktreeState()
    var selectedWorktreePath: String? = null

    val state: Value<WorktreesState> = mutableState

    fun publish() {
        mutableState.value =
            WorktreesState(
                selectedWorktreePath = selectedWorktreePath,
                remoteBranches = buildRemoteBranchItems(),
                createWorktree = createWorktreeState,
            )
    }

    fun findWorktreeByPath(
        path: String,
        repositoriesState: RepositoriesContextState,
    ): Pair<Repository, Worktree>? {
        val normalizedPath = normalizePath(path)
        if (normalizedPath.isBlank()) {
            return null
        }
        repositoriesState.repositories.forEach { repository ->
            val worktree =
                worktreesByRepositoryPath[repository.path]
                    .orEmpty()
                    .firstOrNull { normalizePath(it.path) == normalizedPath }
            if (worktree != null) {
                return repository to worktree
            }
        }
        return null
    }

    fun syncSelectionWithAvailableWorktrees(selectedRepository: Repository?) {
        val selectedRepositoryPath = selectedRepository?.path.orEmpty()
        val availableWorktrees = worktreesByRepositoryPath[selectedRepositoryPath].orEmpty()
        if (selectedWorktreePath != null && availableWorktrees.none { it.path == selectedWorktreePath }) {
            selectedWorktreePath = null
        }
        createWorktreeState =
            createWorktreeState.copy(
                repositoryPath = selectedRepositoryPath,
            )
    }

    private fun buildRemoteBranchItems(): List<RemoteBranchItem> =
        hasRemoteBranchByWorktreePath.map { (worktreePath, hasRemote) ->
            RemoteBranchItem(
                worktreePath = worktreePath,
                hasRemote = hasRemote,
            )
        }
}
