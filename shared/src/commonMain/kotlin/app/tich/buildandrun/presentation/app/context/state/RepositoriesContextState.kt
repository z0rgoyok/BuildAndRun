package app.tich.buildandrun.presentation.app.context.state

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.context.repositories.model.RepositoryGroup

internal class RepositoriesContextState(
    preferencesStore: PreferencesStore,
) {
    var repositories: List<Repository> = emptyList()
    var repositoryGroups: List<RepositoryGroup> = emptyList()
    var addRepositoryPathInput: String = ""
    var selectedRepositoryId: String? = null
    var expandedRepositoryIds: Set<String> = preferencesStore.expandedRepositoryIds
    var collapsedGroupIds: Set<String> = preferencesStore.collapsedGroupIds
}
