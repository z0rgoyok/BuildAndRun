package app.tich.buildandrun.appstore

internal fun AppStoreCore.onSetSidebarRepositoryExpanded(
    repositoryId: String,
    expanded: Boolean,
) {
    val nextExpandedRepositoryIds =
        if (expanded) {
            expandedRepositoryIds + repositoryId
        } else {
            expandedRepositoryIds - repositoryId
        }
    if (nextExpandedRepositoryIds == expandedRepositoryIds) {
        return
    }
    expandedRepositoryIds = nextExpandedRepositoryIds
    persistExpandedRepositoryIds()
    publishState()
}

internal fun AppStoreCore.onSetSidebarGroupCollapsed(
    groupId: String,
    collapsed: Boolean,
) {
    val nextCollapsedGroupIds =
        if (collapsed) {
            collapsedGroupIds + groupId
        } else {
            collapsedGroupIds - groupId
        }
    if (nextCollapsedGroupIds == collapsedGroupIds) {
        return
    }
    collapsedGroupIds = nextCollapsedGroupIds
    persistCollapsedGroupIds()
    publishState()
}

internal fun AppStoreCore.onToggleSidebarRepositoriesExpansion(
    repositoryIds: Set<String>,
    preferredRepositoryId: String?,
) {
    if (repositoryIds.isEmpty()) {
        return
    }

    val nextExpandedRepositoryIds =
        if (areSidebarRepositoriesExpanded(repositoryIds = repositoryIds)) {
            val updated = (expandedRepositoryIds - repositoryIds).toMutableSet()
            val preferredId = preferredRepositoryId?.takeIf { it.isNotBlank() && repositoryIds.contains(it) }
            if (preferredId != null) {
                updated.add(preferredId)
            }
            updated.toSet()
        } else {
            expandedRepositoryIds + repositoryIds
        }

    if (nextExpandedRepositoryIds == expandedRepositoryIds) {
        return
    }
    expandedRepositoryIds = nextExpandedRepositoryIds
    persistExpandedRepositoryIds()
    publishState()
}

internal fun AppStoreCore.onToggleVisibleSidebarRepositoriesExpansion(
    includeArchivedRepositories: Boolean,
    preferredRepositoryId: String?,
) {
    onToggleSidebarRepositoriesExpansion(
        repositoryIds = visibleSidebarRepositoryIds(includeArchivedRepositories = includeArchivedRepositories),
        preferredRepositoryId = preferredRepositoryId,
    )
}

internal fun AppStoreCore.onSyncSidebarSelectionExpansion(repositoryId: String?) {
    val selectedRepositoryId = repositoryId?.takeIf { it.isNotBlank() } ?: return
    if (expandedRepositoryIds.contains(selectedRepositoryId)) {
        return
    }
    expandedRepositoryIds += selectedRepositoryId
    persistExpandedRepositoryIds()
    publishState()
}

internal fun AppStoreCore.areSidebarRepositoriesExpanded(repositoryIds: Set<String>): Boolean =
    repositoryIds.isNotEmpty() && repositoryIds.all(expandedRepositoryIds::contains)

internal fun AppStoreCore.areVisibleSidebarRepositoriesExpanded(includeArchivedRepositories: Boolean): Boolean =
    areSidebarRepositoriesExpanded(
        repositoryIds = visibleSidebarRepositoryIds(includeArchivedRepositories = includeArchivedRepositories),
    )

internal fun AppStoreCore.hasVisibleSidebarRepositories(includeArchivedRepositories: Boolean): Boolean =
    visibleSidebarRepositoryIds(includeArchivedRepositories = includeArchivedRepositories).isNotEmpty()

internal fun AppStoreCore.visibleSidebarRepositoryIds(includeArchivedRepositories: Boolean): Set<String> =
    repositories
        .asSequence()
        .filter { repository ->
            if (repository.isArchived) {
                includeArchivedRepositories
            } else {
                val groupId = repository.groupId?.value
                groupId == null || !collapsedGroupIds.contains(groupId)
            }
        }.map { repository -> repository.id.value }
        .toSet()

internal fun AppStoreCore.persistExpandedRepositoryIds() {
    graph.preferencesStore.expandedRepositoryIds = expandedRepositoryIds
}

internal fun AppStoreCore.persistCollapsedGroupIds() {
    graph.preferencesStore.collapsedGroupIds = collapsedGroupIds
}
