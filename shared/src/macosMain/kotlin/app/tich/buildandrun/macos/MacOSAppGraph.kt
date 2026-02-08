package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.ports.GitClient
import app.tich.buildandrun.domain.ports.PreferencesStore
import app.tich.buildandrun.domain.usecases.AddRepositoryUseCase
import app.tich.buildandrun.domain.usecases.CreateWorktreeUseCase
import app.tich.buildandrun.domain.usecases.LoadRepositoriesUseCase

class MacOSAppGraph {
    val preferencesStore: PreferencesStore = InMemoryPreferencesStore()
    val gitClient: GitClient = MacOSGitClient()
    val addRepositoryUseCase = AddRepositoryUseCase(gitClient = gitClient, preferencesStore = preferencesStore)
    val loadRepositoriesUseCase = LoadRepositoriesUseCase(preferencesStore = preferencesStore)
    val createWorktreeUseCase = CreateWorktreeUseCase(gitClient = gitClient)
}
