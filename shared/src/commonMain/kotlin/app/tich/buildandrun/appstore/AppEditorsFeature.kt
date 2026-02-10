package app.tich.buildandrun.appstore

interface AppEditorsFeature {
    fun onSetRememberEditorChoice(value: Boolean)

    fun onSetEditorEnabled(
        editorId: String,
        enabled: Boolean,
    )

    fun onSetPreferredEditor(editorId: String?)

    fun onOpenInEditor(
        worktreePath: String,
        editorId: String?,
    )

    fun onOpenInFinder(worktreePath: String)

    fun onOpenInTerminal(worktreePath: String)
}
