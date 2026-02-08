package app.tich.buildandrun.macos

import app.tich.buildandrun.appstore.AppStoreGraph
import app.tich.buildandrun.domain.ports.*
import app.tich.buildandrun.domain.usecases.*

internal class MacOSAppGraph : AppStoreGraph {
    override val preferencesStore: PreferencesStore = InMemoryPreferencesStore()
    override val gitClient: GitClient = MacOSGitClient()
    override val fileSystem: FileSystemHandling = MacOSFileSystemHandling()
    override val editorOpening: EditorOpening = MacOSEditorOpening()
    override val systemOpening: SystemOpening = MacOSSystemOpening()
    override val addRepositoryUseCase = AddRepositoryUseCase(gitClient = gitClient, preferencesStore = preferencesStore)
    override val loadRepositoriesUseCase = LoadRepositoriesUseCase(preferencesStore = preferencesStore)
    override val createWorktreeUseCase = CreateWorktreeUseCase(gitClient = gitClient)
    override val removeRepositoryUseCase = RemoveRepositoryUseCase(preferencesStore = preferencesStore)
    override val setRepositoryArchivedStateUseCase = SetRepositoryArchivedStateUseCase(preferencesStore = preferencesStore)
    override val loadBranchesUseCase = LoadBranchesUseCase(gitClient = gitClient)
}
