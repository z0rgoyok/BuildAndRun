package app.tich.buildandrun.appstore

internal class AppSidebarService(
    private val runtime: AppRuntime,
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
