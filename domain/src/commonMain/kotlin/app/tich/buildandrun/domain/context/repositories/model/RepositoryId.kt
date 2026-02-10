package app.tich.buildandrun.domain.context.repositories.model

import app.tich.buildandrun.domain.shared.model.generateUuid

value class RepositoryId(val value: String) {
    init {
        require(value.isNotBlank()) { "RepositoryId cannot be blank" }
    }

    companion object {
        fun generate(): RepositoryId = RepositoryId(generateUuid())
    }
}
