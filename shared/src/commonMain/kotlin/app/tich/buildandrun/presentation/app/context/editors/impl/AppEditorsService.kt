package app.tich.buildandrun.presentation.app.context.editors.impl

import app.tich.buildandrun.presentation.app.AppEditorsFeature
import app.tich.buildandrun.presentation.app.core.AppRuntime

internal class AppEditorsService(
    private val runtime: AppRuntime,
) : AppEditorsFeature {
    override fun onSetRememberEditorChoice(value: Boolean) {
        runtime.onSetRememberEditorChoice(value = value)
    }

    override fun onSetEditorEnabled(
        editorId: String,
        enabled: Boolean,
    ) {
        runtime.onSetEditorEnabled(
            editorId = editorId,
            enabled = enabled,
        )
    }

    override fun onSetPreferredEditor(editorId: String?) {
        runtime.onSetPreferredEditor(editorId = editorId)
    }

    override fun onOpenInEditor(
        worktreePath: String,
        editorId: String?,
    ) {
        runtime.onOpenInEditor(
            worktreePath = worktreePath,
            editorId = editorId,
        )
    }

    override fun onOpenInFinder(worktreePath: String) {
        runtime.onOpenInFinder(worktreePath = worktreePath)
    }

    override fun onOpenInTerminal(worktreePath: String) {
        runtime.onOpenInTerminal(worktreePath = worktreePath)
    }
}
