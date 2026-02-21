package app.tich.buildandrun.presentation.app

data class MessagesState(
    val error: ErrorState? = null,
    val success: SuccessState? = null,
)
