package app.tich.buildandrun.macos

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.repositories.usecase.*
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.port.FileSystemHandling
import app.tich.buildandrun.application.context.shared.port.SystemOpening
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.application.context.worktrees.usecase.CreateWorktreeUseCase
import app.tich.buildandrun.application.context.worktrees.usecase.LoadBranchesUseCase
import app.tich.buildandrun.presentation.app.core.AppStoreGraph
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun macosPlatformModule(): Module =
    module {
        single<PreferencesStore> { MacOSPreferencesStore() }
        single<GitClient> { MacOSGitClient() }
        single<FileSystemHandling> { MacOSFileSystemHandling() }
        single<EditorOpening> { MacOSEditorOpening() }
        single<SystemOpening> { MacOSSystemOpening() }

        single { AddRepositoryUseCase(gitClient = get(), preferencesStore = get()) }
        single { LoadRepositoriesUseCase(preferencesStore = get()) }
        single { CreateWorktreeUseCase(gitClient = get()) }
        single { RemoveRepositoryUseCase(preferencesStore = get()) }
        single { SetRepositoryArchivedStateUseCase(preferencesStore = get()) }
        single { SetRepositoryGroupUseCase(preferencesStore = get()) }
        single { LoadBranchesUseCase(gitClient = get()) }

        single<AppStoreGraph> {
            MacOSAppGraph(
                preferencesStore = get(),
                gitClient = get(),
                fileSystem = get(),
                editorOpening = get(),
                systemOpening = get(),
                addRepositoryUseCase = get(),
                loadRepositoriesUseCase = get(),
                createWorktreeUseCase = get(),
                removeRepositoryUseCase = get(),
                setRepositoryArchivedStateUseCase = get(),
                setRepositoryGroupUseCase = get(),
                loadBranchesUseCase = get(),
            )
        }
    }
