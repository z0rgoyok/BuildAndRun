package app.tich.buildandrun.domain.context.repositories.model

data class RepositoryGroup(
    val id: RepositoryGroupId,
    val name: String,
    val sortOrder: Int,
) {
    init {
        require(name.isNotBlank()) { "RepositoryGroup name cannot be blank" }
        require(sortOrder >= 0) { "RepositoryGroup sortOrder must be non-negative" }
    }

    companion object {
        fun create(
            name: String,
            sortOrder: Int,
        ): RepositoryGroup =
            RepositoryGroup(
                id = RepositoryGroupId.generate(),
                name = name.trim(),
                sortOrder = sortOrder,
            )
    }
}
