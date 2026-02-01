package app.tich.buildandrun.domain.entities

data class PRStatus(
    val number: Int,
    val state: PRState,
    val url: String,
    val title: String?,
) {
    init {
        require(number > 0) { "PR number must be positive" }
        require(url.isNotBlank()) { "PR URL cannot be blank" }
    }
}
