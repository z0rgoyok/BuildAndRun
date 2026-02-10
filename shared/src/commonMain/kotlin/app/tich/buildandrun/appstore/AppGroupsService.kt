package app.tich.buildandrun.appstore

internal class AppGroupsService(
    private val runtime: AppRuntime,
) : AppGroupsFeature {
    override fun onReorderRepositoryGroups(orderedGroupIds: List<String>) {
        runtime.onReorderRepositoryGroups(orderedGroupIds = orderedGroupIds)
    }

    override fun onCreateRepositoryGroup(name: String) {
        runtime.onCreateRepositoryGroup(name = name)
    }

    override fun onRenameRepositoryGroup(
        groupId: String,
        newName: String,
    ) {
        runtime.onRenameRepositoryGroup(
            groupId = groupId,
            newName = newName,
        )
    }

    override fun onDeleteRepositoryGroup(groupId: String) {
        runtime.onDeleteRepositoryGroup(groupId = groupId)
    }

    override fun onSetRepositoryGroup(
        repositoryId: String,
        groupId: String?,
    ) {
        runtime.onSetRepositoryGroup(
            repositoryId = repositoryId,
            groupId = groupId,
        )
    }

    override fun onCreateGroupAndAssignRepository(
        name: String,
        repositoryId: String,
    ) {
        runtime.onCreateGroupAndAssignRepository(
            name = name,
            repositoryId = repositoryId,
        )
    }
}
