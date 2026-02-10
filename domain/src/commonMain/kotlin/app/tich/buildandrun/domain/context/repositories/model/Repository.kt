package app.tich.buildandrun.domain.context.repositories.model

data class Repository(
    val id: RepositoryId,
    val path: String,
    val name: String,
    val isArchived: Boolean,
    val groupId: RepositoryGroupId? = null,
) {
    init {
        require(path.isNotBlank()) { "Repository path cannot be blank" }
        require(name.isNotBlank()) { "Repository name cannot be blank" }
    }

    companion object {
        fun create(
            path: String,
            name: String? = null,
            isArchived: Boolean = false,
            groupId: RepositoryGroupId? = null,
        ): Repository {
            val derivedName = name ?: path.substringAfterLast('/')
            return Repository(
                id = RepositoryId.generate(),
                path = path,
                name = derivedName,
                isArchived = isArchived,
                groupId = groupId,
            )
        }
    }
}
