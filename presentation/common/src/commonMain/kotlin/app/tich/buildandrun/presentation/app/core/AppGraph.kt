package app.tich.buildandrun.presentation.app.core

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.port.FileSystemHandling
import app.tich.buildandrun.application.context.shared.port.SystemOpening
import app.tich.buildandrun.application.context.worktrees.port.GitClient

interface AppGraph {
    val preferencesStore: PreferencesStore
    val gitClient: GitClient
    val fileSystem: FileSystemHandling
    val editorOpening: EditorOpening
    val systemOpening: SystemOpening
}
