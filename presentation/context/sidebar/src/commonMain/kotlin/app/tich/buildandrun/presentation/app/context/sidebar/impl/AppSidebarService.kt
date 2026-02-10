package app.tich.buildandrun.presentation.app.context.sidebar.impl

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.presentation.app.AppSidebarFeature
import app.tich.buildandrun.presentation.app.context.state.RepositoriesContextState
import app.tich.buildandrun.presentation.app.core.AppStateRefresher

class AppSidebarService(
    private val stateRefresher: AppStateRefresher,
    private val repositoriesState: RepositoriesContextState,
    private val preferencesStore: PreferencesStore,
) : AppSidebarFeature {
    override fun onSetSidebarRepositoryExpanded(
        repositoryId: String,
        expanded: Boolean,
    ) {
        updateSet(
            current = repositoriesState.expandedRepositoryIds,
            value = repositoryId,
            enabled = expanded,
        ) { nextExpandedRepositoryIds ->
            repositoriesState.expandedRepositoryIds = nextExpandedRepositoryIds
            preferencesStore.expandedRepositoryIds = nextExpandedRepositoryIds
        }
    }

    override fun onSetSidebarGroupCollapsed(
        groupId: String,
        collapsed: Boolean,
    ) {
        updateSet(
            current = repositoriesState.collapsedGroupIds,
            value = groupId,
            enabled = collapsed,
        ) { nextCollapsedGroupIds ->
            repositoriesState.collapsedGroupIds = nextCollapsedGroupIds
            preferencesStore.collapsedGroupIds = nextCollapsedGroupIds
        }
    }

    override fun onToggleVisibleSidebarRepositoriesExpansion(
        includeArchivedRepositories: Boolean,
        preferredRepositoryId: String?,
    ) {
        onToggleSidebarRepositoriesExpansion(
            repositoryIds = visibleSidebarRepositoryIds(includeArchivedRepositories = includeArchivedRepositories),
            preferredRepositoryId = preferredRepositoryId,
        )
    }

    override fun onSyncSidebarSelectionExpansion(repositoryId: String?) {
        val selectedRepositoryId = repositoryId?.takeIf { it.isNotBlank() } ?: return
        if (repositoriesState.expandedRepositoryIds.contains(selectedRepositoryId)) {
            return
        }
        repositoriesState.expandedRepositoryIds += selectedRepositoryId
        preferencesStore.expandedRepositoryIds = repositoriesState.expandedRepositoryIds
        stateRefresher.publishAll()
    }

    override fun areVisibleSidebarRepositoriesExpanded(includeArchivedRepositories: Boolean): Boolean =
        areSidebarRepositoriesExpanded(
            repositoryIds = visibleSidebarRepositoryIds(includeArchivedRepositories = includeArchivedRepositories),
        )

    override fun hasVisibleSidebarRepositories(includeArchivedRepositories: Boolean): Boolean =
        visibleSidebarRepositoryIds(includeArchivedRepositories = includeArchivedRepositories).isNotEmpty()

    private fun onToggleSidebarRepositoriesExpansion(
        repositoryIds: Set<String>,
        preferredRepositoryId: String?,
    ) {
        if (repositoryIds.isEmpty()) {
            return
        }

        val nextExpandedRepositoryIds =
            if (areSidebarRepositoriesExpanded(repositoryIds = repositoryIds)) {
                val updated = (repositoriesState.expandedRepositoryIds - repositoryIds).toMutableSet()
                val preferredId = preferredRepositoryId?.takeIf { it.isNotBlank() && repositoryIds.contains(it) }
                if (preferredId != null) {
                    updated.add(preferredId)
                }
                updated.toSet()
            } else {
                repositoriesState.expandedRepositoryIds + repositoryIds
            }

        if (nextExpandedRepositoryIds == repositoriesState.expandedRepositoryIds) {
            return
        }
        repositoriesState.expandedRepositoryIds = nextExpandedRepositoryIds
        preferencesStore.expandedRepositoryIds = repositoriesState.expandedRepositoryIds
        stateRefresher.publishAll()
    }

    private fun areSidebarRepositoriesExpanded(repositoryIds: Set<String>): Boolean =
        repositoryIds.isNotEmpty() && repositoryIds.all(repositoriesState.expandedRepositoryIds::contains)

    private fun updateSet(
        current: Set<String>,
        value: String,
        enabled: Boolean,
        onUpdated: (Set<String>) -> Unit,
    ) {
        val next =
            if (enabled) {
                current + value
            } else {
                current - value
            }
        if (next == current) {
            return
        }
        onUpdated(next)
        stateRefresher.publishAll()
    }

    private fun visibleSidebarRepositoryIds(includeArchivedRepositories: Boolean): Set<String> =
        repositoriesState.repositories
            .asSequence()
            .filter { repository ->
                if (repository.isArchived) {
                    includeArchivedRepositories
                } else {
                    val groupId = repository.groupId?.value
                    groupId == null || !repositoriesState.collapsedGroupIds.contains(groupId)
                }
            }.map { repository -> repository.id.value }
            .toSet()
}
