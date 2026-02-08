package app.tich.buildandrun.presentation.i18n

sealed class UiText {
    data class Key(
        val key: String,
        val args: List<String> = emptyList(),
        val quantity: Int? = null,
    ) : UiText() {
        init {
            require(key.isNotBlank()) { "UiText key cannot be blank" }
        }
    }
}
