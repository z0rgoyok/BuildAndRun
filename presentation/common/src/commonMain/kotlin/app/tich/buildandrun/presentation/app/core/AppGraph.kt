package app.tich.buildandrun.presentation.app.core

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.repositories.usecase.*
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.port.FileSystemHandling
import app.tich.buildandrun.application.context.shared.port.SystemOpening
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.application.context.worktrees.usecase.CreateWorktreeUseCase
import app.tich.buildandrun.application.context.worktrees.usecase.LoadBranchesUseCase

interface AppGraph {
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
    val setRepositoryGroupUseCase: SetRepositoryGroupUseCase
    val loadBranchesUseCase: LoadBranchesUseCase
}
