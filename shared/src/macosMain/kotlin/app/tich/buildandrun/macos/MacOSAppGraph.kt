package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.ports.GitClient
import app.tich.buildandrun.domain.ports.PreferencesStore
import app.tich.buildandrun.domain.usecases.*

class MacOSAppGraph {
    val preferencesStore: PreferencesStore = InMemoryPreferencesStore()
    val gitClient: GitClient = MacOSGitClient()
    val addRepositoryUseCase = AddRepositoryUseCase(gitClient = gitClient, preferencesStore = preferencesStore)
    val loadRepositoriesUseCase = LoadRepositoriesUseCase(preferencesStore = preferencesStore)
    val createWorktreeUseCase = CreateWorktreeUseCase(gitClient = gitClient)
    val removeRepositoryUseCase = RemoveRepositoryUseCase(preferencesStore = preferencesStore)
    val setRepositoryArchivedStateUseCase = SetRepositoryArchivedStateUseCase(preferencesStore = preferencesStore)
}
