package app.tich.buildandrun.appstore

interface AppSidebarFeature {
    fun onSetSidebarRepositoryExpanded(
        repositoryId: String,
        expanded: Boolean,
    )

    fun onSetSidebarGroupCollapsed(
        groupId: String,
        collapsed: Boolean,
    )

    fun onToggleVisibleSidebarRepositoriesExpansion(
        includeArchivedRepositories: Boolean,
        preferredRepositoryId: String?,
    )

    fun onSyncSidebarSelectionExpansion(repositoryId: String?)

    fun areVisibleSidebarRepositoriesExpanded(includeArchivedRepositories: Boolean): Boolean

    fun hasVisibleSidebarRepositories(includeArchivedRepositories: Boolean): Boolean
}
