package app.tich.buildandrun.domain.entities

data class Repository(
    val id: RepositoryId,
    val path: String,
    val name: String,
) {
    init {
        require(path.isNotBlank()) { "Repository path cannot be blank" }
        require(name.isNotBlank()) { "Repository name cannot be blank" }
    }

    companion object {
        fun create(
            path: String,
            name: String? = null,
        ): Repository {
            val derivedName = name ?: path.substringAfterLast('/')
            return Repository(
                id = RepositoryId.generate(),
                path = path,
                name = derivedName,
            )
        }
    }
}
