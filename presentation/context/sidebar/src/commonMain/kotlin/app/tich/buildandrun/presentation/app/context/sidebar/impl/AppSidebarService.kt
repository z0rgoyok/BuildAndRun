package app.tich.buildandrun.presentation.app.context.sidebar.impl

import app.tich.buildandrun.presentation.app.AppSidebarFeature
import app.tich.buildandrun.presentation.app.core.AppWiring

class AppSidebarService(
    private val runtime: AppWiring,
) : AppSidebarFeature {
    override fun onSetSidebarRepositoryExpanded(
        repositoryId: String,
        expanded: Boolean,
    ) {
        runtime.onSetSidebarRepositoryExpanded(
            repositoryId = repositoryId,
            expanded = expanded,
        )
    }

    override fun onSetSidebarGroupCollapsed(
        groupId: String,
        collapsed: Boolean,
    ) {
        runtime.onSetSidebarGroupCollapsed(
            groupId = groupId,
            collapsed = collapsed,
        )
    }

    override fun onToggleVisibleSidebarRepositoriesExpansion(
        includeArchivedRepositories: Boolean,
        preferredRepositoryId: String?,
    ) {
        runtime.onToggleVisibleSidebarRepositoriesExpansion(
            includeArchivedRepositories = includeArchivedRepositories,
            preferredRepositoryId = preferredRepositoryId,
        )
    }

    override fun onSyncSidebarSelectionExpansion(repositoryId: String?) {
        runtime.onSyncSidebarSelectionExpansion(repositoryId = repositoryId)
    }

    override fun areVisibleSidebarRepositoriesExpanded(includeArchivedRepositories: Boolean): Boolean =
        runtime.areVisibleSidebarRepositoriesExpanded(includeArchivedRepositories = includeArchivedRepositories)

    override fun hasVisibleSidebarRepositories(includeArchivedRepositories: Boolean): Boolean =
        runtime.hasVisibleSidebarRepositories(includeArchivedRepositories = includeArchivedRepositories)
}
