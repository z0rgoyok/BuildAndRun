package app.tich.buildandrun.domain.context.copy.model

data class CopyPattern(
    val pattern: String,
) {
    val id: String get() = pattern
    val isDirectory: Boolean get() = pattern.endsWith("/")

    init {
        require(pattern.isNotBlank()) { "Copy pattern cannot be blank" }
    }
}
