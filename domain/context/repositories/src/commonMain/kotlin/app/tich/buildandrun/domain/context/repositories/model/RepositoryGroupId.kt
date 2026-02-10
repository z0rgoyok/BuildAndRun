package app.tich.buildandrun.domain.context.repositories.model

import app.tich.buildandrun.domain.shared.model.generateUuid

value class RepositoryGroupId(val value: String) {
    init {
        require(value.isNotBlank()) { "RepositoryGroupId cannot be blank" }
    }

    companion object {
        fun generate(): RepositoryGroupId = RepositoryGroupId(generateUuid())
    }
}
