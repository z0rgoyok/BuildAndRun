package app.tich.buildandrun.presentation.app

data class SidebarSection(
    val groupId: String?,
    val groupName: String?,
    val repositories: List<RepositoryItem>,
)
