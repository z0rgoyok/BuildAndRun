package app.tich.buildandrun.presentation.app.context.state

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.domain.context.copy.model.CopyPattern

class SettingsContextState(
    preferencesStore: PreferencesStore,
) {
    var branches: List<String> = emptyList()
    var worktreeBasePath: String = preferencesStore.worktreeBasePath
    var defaultCopyPatterns: List<CopyPattern> = preferencesStore.defaultCopyPatterns
}
