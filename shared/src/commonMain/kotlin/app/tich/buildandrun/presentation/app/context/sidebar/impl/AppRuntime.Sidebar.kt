package app.tich.buildandrun.presentation.app.context.sidebar.impl

import app.tich.buildandrun.presentation.app.core.AppRuntime
import app.tich.buildandrun.presentation.app.core.publishState

internal fun AppRuntime.onSetSidebarRepositoryExpanded(
    repositoryId: String,
    expanded: Boolean,
) {
    val nextExpandedRepositoryIds =
        if (expanded) {
            repositoriesState.expandedRepositoryIds + repositoryId
        } else {
            repositoriesState.expandedRepositoryIds - repositoryId
        }
    if (nextExpandedRepositoryIds == repositoriesState.expandedRepositoryIds) {
        return
    }
    repositoriesState.expandedRepositoryIds = nextExpandedRepositoryIds
    persistExpandedRepositoryIds()
    publishState()
}

internal fun AppRuntime.onSetSidebarGroupCollapsed(
    groupId: String,
    collapsed: Boolean,
) {
    val nextCollapsedGroupIds =
        if (collapsed) {
            repositoriesState.collapsedGroupIds + groupId
        } else {
            repositoriesState.collapsedGroupIds - groupId
        }
    if (nextCollapsedGroupIds == repositoriesState.collapsedGroupIds) {
        return
    }
    repositoriesState.collapsedGroupIds = nextCollapsedGroupIds
    persistCollapsedGroupIds()
    publishState()
}

internal fun AppRuntime.onToggleSidebarRepositoriesExpansion(
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
    persistExpandedRepositoryIds()
    publishState()
}

internal fun AppRuntime.onToggleVisibleSidebarRepositoriesExpansion(
    includeArchivedRepositories: Boolean,
    preferredRepositoryId: String?,
) {
    onToggleSidebarRepositoriesExpansion(
        repositoryIds = visibleSidebarRepositoryIds(includeArchivedRepositories = includeArchivedRepositories),
        preferredRepositoryId = preferredRepositoryId,
    )
}

internal fun AppRuntime.onSyncSidebarSelectionExpansion(repositoryId: String?) {
    val selectedRepositoryId = repositoryId?.takeIf { it.isNotBlank() } ?: return
    if (repositoriesState.expandedRepositoryIds.contains(selectedRepositoryId)) {
        return
    }
    repositoriesState.expandedRepositoryIds += selectedRepositoryId
    persistExpandedRepositoryIds()
    publishState()
}

internal fun AppRuntime.areSidebarRepositoriesExpanded(repositoryIds: Set<String>): Boolean =
    repositoryIds.isNotEmpty() && repositoryIds.all(repositoriesState.expandedRepositoryIds::contains)

internal fun AppRuntime.areVisibleSidebarRepositoriesExpanded(includeArchivedRepositories: Boolean): Boolean =
    areSidebarRepositoriesExpanded(
        repositoryIds = visibleSidebarRepositoryIds(includeArchivedRepositories = includeArchivedRepositories),
    )

internal fun AppRuntime.hasVisibleSidebarRepositories(includeArchivedRepositories: Boolean): Boolean =
    visibleSidebarRepositoryIds(includeArchivedRepositories = includeArchivedRepositories).isNotEmpty()

internal fun AppRuntime.visibleSidebarRepositoryIds(includeArchivedRepositories: Boolean): Set<String> =
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

internal fun AppRuntime.persistExpandedRepositoryIds() {
    graph.preferencesStore.expandedRepositoryIds = repositoriesState.expandedRepositoryIds
}

internal fun AppRuntime.persistCollapsedGroupIds() {
    graph.preferencesStore.collapsedGroupIds = repositoriesState.collapsedGroupIds
}
