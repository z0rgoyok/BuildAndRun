package app.tich.buildandrun.domain.entities

data class CopyFailure(
    val path: String,
    val error: String,
)
