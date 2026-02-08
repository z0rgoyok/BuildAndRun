package app.tich.buildandrun.appstore

import app.tich.buildandrun.application.ports.*
import app.tich.buildandrun.application.usecases.*

internal interface AppStoreGraph {
    val preferencesStore: PreferencesStore
    val gitClient: GitClient
    val fileSystem: FileSystemHandling
    val editorOpening: EditorOpening
    val systemOpening: SystemOpening
    val addRepositoryUseCase: AddRepositoryUseCase
    val loadRepositoriesUseCase: LoadRepositoriesUseCase
    val createWorktreeUseCase: CreateWorktreeUseCase
    val removeRepositoryUseCase: RemoveRepositoryUseCase
    val setRepositoryArchivedStateUseCase: SetRepositoryArchivedStateUseCase
    val loadBranchesUseCase: LoadBranchesUseCase
}
