package app.tich.buildandrun.presentation.app.context.state

import app.tich.buildandrun.domain.context.editors.model.Editor
import app.tich.buildandrun.presentation.app.EditorItem
import app.tich.buildandrun.presentation.app.EditorsState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

class EditorsContextState {
    private val mutableState = MutableValue(EditorsState())

    var rememberEditorChoice: Boolean = true
    var enabledEditorIds: Set<String>? = null
    var allEditors: List<Editor> = emptyList()
    val installedEditorIds: MutableSet<String> = mutableSetOf()
    var editorItems: List<EditorItem> = emptyList()
    var preferredEditorId: String? = null

    val state: Value<EditorsState> = mutableState

    fun publish() {
        mutableState.value =
            EditorsState(
                rememberEditorChoice = rememberEditorChoice,
                preferredEditorId = preferredEditorId,
                editors = editorItems,
            )
    }
}
