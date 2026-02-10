package app.tich.buildandrun.application.context.shared.port

import app.tich.buildandrun.domain.context.editors.model.Editor

interface EditorOpening {
    suspend fun open(
        path: String,
        withEditor: Editor,
    )

    fun availableEditors(): List<Editor>

    fun allEditors(): List<Editor>

    fun isInstalled(editor: Editor): Boolean
}
