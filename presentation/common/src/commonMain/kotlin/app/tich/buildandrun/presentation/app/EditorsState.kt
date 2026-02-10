package app.tich.buildandrun.presentation.app

data class EditorsState(
    val rememberEditorChoice: Boolean = true,
    val preferredEditorId: String? = null,
    val editors: List<EditorItem> = emptyList(),
)
