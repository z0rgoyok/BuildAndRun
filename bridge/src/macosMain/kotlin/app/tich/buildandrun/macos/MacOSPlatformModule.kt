package app.tich.buildandrun.macos

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.port.FileSystemHandling
import app.tich.buildandrun.application.context.shared.port.SystemOpening
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.presentation.app.core.AppGraph
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun macosPlatformModule(): Module =
    module {
        single<PreferencesStore> { MacOSPreferencesStore() }
        single<GitClient> { MacOSGitClient() }
        single<FileSystemHandling> { MacOSFileSystemHandling() }
        single<EditorOpening> { MacOSEditorOpening() }
        single<SystemOpening> { MacOSSystemOpening() }

        single<AppGraph> {
            MacOSAppGraph(
                preferencesStore = get(),
                gitClient = get(),
                fileSystem = get(),
                editorOpening = get(),
                systemOpening = get(),
            )
        }
    }
