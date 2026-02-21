package app.tich.buildandrun.presentation.app

data class RepositoriesState(
    val repositories: List<RepositoryItem> = emptyList(),
    val sidebarSections: List<SidebarSection> = emptyList(),
    val expandedRepositoryIds: Set<String> = emptySet(),
    val collapsedGroupIds: Set<String> = emptySet(),
    val repositoryGroups: List<RepositoryGroupItem> = emptyList(),
    val selectedRepositoryId: String? = null,
    val addRepositoryPathInput: String = "",
)
