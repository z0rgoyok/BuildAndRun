package app.tich.buildandrun.application.context.shared.usecase

import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.domain.context.editors.model.Editor

class LoadInstalledEditorsUseCase(
    private val editorOpening: EditorOpening,
) {
    fun execute(): UseCaseResult<Output> {
        return runCatchingCancellable {
            val allEditors = editorOpening.allEditors()
            val installedEditorIds =
                allEditors
                    .asSequence()
                    .filter { editor -> editorOpening.isInstalled(editor = editor) }
                    .map(Editor::id)
                    .toSet()
            Output(
                allEditors = allEditors,
                installedEditorIds = installedEditorIds,
            )
        }.toUseCaseResult()
    }

    data class Output(
        val allEditors: List<Editor>,
        val installedEditorIds: Set<String>,
    )
}
