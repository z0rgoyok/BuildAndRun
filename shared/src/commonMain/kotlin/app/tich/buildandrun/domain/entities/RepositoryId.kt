package app.tich.buildandrun.domain.entities

import kotlin.jvm.JvmInline

@JvmInline
value class RepositoryId(val value: String) {
    init {
        require(value.isNotBlank()) { "RepositoryId cannot be blank" }
    }

    companion object {
        fun generate(): RepositoryId = RepositoryId(generateUuid())
    }
}
