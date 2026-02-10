package app.tich.buildandrun.domain.context.copy.model

data class CopyFailure(
    val path: String,
    val error: String,
)
