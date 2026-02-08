package app.tich.buildandrun.application.ports

import app.tich.buildandrun.domain.entities.Editor

interface EditorOpening {
    suspend fun open(
        path: String,
        withEditor: Editor,
    )

    fun availableEditors(): List<Editor>

    fun allEditors(): List<Editor>

    fun isInstalled(editor: Editor): Boolean
}
