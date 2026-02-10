package app.tich.buildandrun.macos

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.repositories.usecase.*
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.port.FileSystemHandling
import app.tich.buildandrun.application.context.shared.port.SystemOpening
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.application.context.worktrees.usecase.CreateWorktreeUseCase
import app.tich.buildandrun.application.context.worktrees.usecase.LoadBranchesUseCase
import app.tich.buildandrun.presentation.app.core.AppGraph

internal class MacOSAppGraph(
    override val preferencesStore: PreferencesStore,
    override val gitClient: GitClient,
    override val fileSystem: FileSystemHandling,
    override val editorOpening: EditorOpening,
    override val systemOpening: SystemOpening,
    override val addRepositoryUseCase: AddRepositoryUseCase,
    override val loadRepositoriesUseCase: LoadRepositoriesUseCase,
    override val createWorktreeUseCase: CreateWorktreeUseCase,
    override val removeRepositoryUseCase: RemoveRepositoryUseCase,
    override val setRepositoryArchivedStateUseCase: SetRepositoryArchivedStateUseCase,
    override val setRepositoryGroupUseCase: SetRepositoryGroupUseCase,
    override val loadBranchesUseCase: LoadBranchesUseCase,
) : AppGraph
