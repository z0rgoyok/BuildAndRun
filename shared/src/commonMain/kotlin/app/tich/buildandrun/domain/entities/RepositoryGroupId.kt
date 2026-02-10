package app.tich.buildandrun.domain.entities

value class RepositoryGroupId(val value: String) {
    init {
        require(value.isNotBlank()) { "RepositoryGroupId cannot be blank" }
    }

    companion object {
        fun generate(): RepositoryGroupId = RepositoryGroupId(generateUuid())
    }
}
