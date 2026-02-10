package app.tich.buildandrun.presentation.app.context.state

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.domain.context.editors.model.Editor

internal class EditorsContextState(
    preferencesStore: PreferencesStore,
    editorOpening: EditorOpening,
) {
    var rememberEditorChoice: Boolean = preferencesStore.rememberEditorChoice
    var enabledEditorIds: Set<String>? = preferencesStore.enabledEditorIds
    val allEditors: List<Editor> = editorOpening.allEditors()
    val installedEditorIds: MutableSet<String> = mutableSetOf()
}
