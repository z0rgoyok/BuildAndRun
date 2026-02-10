package app.tich.buildandrun.presentation.app

interface AppGroupsFeature {
    fun onReorderRepositoryGroups(orderedGroupIds: List<String>)

    fun onCreateRepositoryGroup(name: String)

    fun onRenameRepositoryGroup(
        groupId: String,
        newName: String,
    )

    fun onDeleteRepositoryGroup(groupId: String)

    fun onSetRepositoryGroup(
        repositoryId: String,
        groupId: String?,
    )

    fun onCreateGroupAndAssignRepository(
        name: String,
        repositoryId: String,
    )
}
