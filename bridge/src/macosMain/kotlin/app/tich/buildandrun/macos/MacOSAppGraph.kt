package app.tich.buildandrun.macos

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.port.FileSystemHandling
import app.tich.buildandrun.application.context.shared.port.SystemOpening
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.presentation.app.core.AppGraph

internal class MacOSAppGraph(
    override val preferencesStore: PreferencesStore,
    override val gitClient: GitClient,
    override val fileSystem: FileSystemHandling,
    override val editorOpening: EditorOpening,
    override val systemOpening: SystemOpening,
) : AppGraph
