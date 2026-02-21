package app.tich.buildandrun.presentation.app.context.state

import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.context.repositories.model.RepositoryGroup
import app.tich.buildandrun.domain.context.worktrees.model.Worktree
import app.tich.buildandrun.presentation.app.*
import app.tich.buildandrun.presentation.app.core.ActivityCenter
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

class RepositoriesContextState {
    private val mutableState = MutableValue(RepositoriesState())

    var repositories: List<Repository> = emptyList()
    var repositoryGroups: List<RepositoryGroup> = emptyList()
    var addRepositoryPathInput: String = ""
    var selectedRepositoryId: String? = null
    var expandedRepositoryIds: Set<String> = emptySet()
    var collapsedGroupIds: Set<String> = emptySet()

    val state: Value<RepositoriesState> = mutableState

    fun selectedRepository(): Repository? = repositories.firstOrNull { it.id.value == selectedRepositoryId }

    fun preferredSelectedRepository(): Repository? = repositories.firstOrNull { !it.isArchived } ?: repositories.firstOrNull()

    fun preferredSelectedRepositoryId(): String? = preferredSelectedRepository()?.id?.value

    fun publish(
        worktreesState: WorktreesContextState,
        activityCenter: ActivityCenter,
    ) {
        val repositoryItems = buildRepositoryItems(worktreesState = worktreesState, activityCenter = activityCenter)
        mutableState.value =
            RepositoriesState(
                repositories = repositoryItems,
                sidebarSections = buildSidebarSections(repositoryItems = repositoryItems),
                expandedRepositoryIds = expandedRepositoryIds,
                collapsedGroupIds = collapsedGroupIds,
                repositoryGroups = buildRepositoryGroupItems(),
                selectedRepositoryId = selectedRepositoryId,
                addRepositoryPathInput = addRepositoryPathInput,
            )
    }

    private fun buildRepositoryItems(
        worktreesState: WorktreesContextState,
        activityCenter: ActivityCenter,
    ): List<RepositoryItem> =
        repositories.map { repository ->
            RepositoryItem(
                id = repository.id.value,
                name = repository.name,
                path = repository.path,
                isArchived = repository.isArchived,
                groupId = repository.groupId?.value,
                worktrees =
                    worktreesState.worktreesByRepositoryPath[repository.path]
                        .orEmpty()
                        .sortedWith(
                            compareByDescending<Worktree> { it.isMain }
                                .thenBy { it.name.lowercase() },
                        ).map {
                            WorktreeItem(
                                path = it.path,
                                name = it.name,
                                branch = it.branch,
                                isDetachedHead = it.isDetachedHead,
                                baseBranch = it.baseBranch,
                                isMain = it.isMain,
                                isLocked = it.isLocked,
                                isPrunable = it.isPrunable,
                                status = worktreesState.worktreeStatusByPath[it.path],
                                isStatusLoading =
                                    worktreesState.worktreeStatusLoadingPaths.contains(it.path) ||
                                        activityCenter.isWorktreeActive(it.path),
                            )
                        },
            )
        }

    private fun buildSidebarSections(repositoryItems: List<RepositoryItem>): List<SidebarSection> {
        val activeRepos = repositoryItems.filter { !it.isArchived }
        val ungrouped = activeRepos.filter { it.groupId == null }
        val groupedByGroupId = activeRepos.filter { it.groupId != null }.groupBy { it.groupId }
        val sortedGroups = repositoryGroups.sortedBy { it.sortOrder }

        val sections = mutableListOf<SidebarSection>()
        if (ungrouped.isNotEmpty()) {
            sections +=
                SidebarSection(
                    groupId = null,
                    groupName = null,
                    repositories = ungrouped,
                )
        }
        for (group in sortedGroups) {
            val repos = groupedByGroupId[group.id.value].orEmpty()
            sections +=
                SidebarSection(
                    groupId = group.id.value,
                    groupName = group.name,
                    repositories = repos,
                )
        }
        return sections
    }

    private fun buildRepositoryGroupItems(): List<RepositoryGroupItem> =
        repositoryGroups
            .sortedBy { it.sortOrder }
            .map { group ->
                RepositoryGroupItem(
                    id = group.id.value,
                    name = group.name,
                )
            }
}
