package app.tich.buildandrun.appstore

import app.tich.buildandrun.application.usecases.UseCaseResult
import app.tich.buildandrun.resources.Res
import app.tich.buildandrun.resources.loading_initial
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal fun AppStoreCore.loadInitial() {
    scope.launch {
        withGlobalLoading(Res.string.loading_initial) {
            when (val result = graph.loadRepositoriesUseCase.execute()) {
                is UseCaseResult.Success -> {
                    repositories = result.value
                    selectedRepositoryId =
                        graph.preferencesStore.lastSelectedRepositoryId
                            ?.takeIf { selectedId -> repositories.any { it.id.value == selectedId } }
                            ?: preferredSelectedRepositoryId()
                    selectedWorktreePath = graph.preferencesStore.lastSelectedWorktreePath
                    worktreeBasePath = graph.preferencesStore.worktreeBasePath
                    defaultCopyPatterns = graph.preferencesStore.defaultCopyPatterns
                    rememberEditorChoice = graph.preferencesStore.rememberEditorChoice
                    enabledEditorIds = graph.preferencesStore.enabledEditorIds
                }

                is UseCaseResult.Failure -> {
                    error = mapFailureToErrorState(result.value)
                }
            }
            refreshInstalledEditors()
            repositories.forEach { repository ->
                loadWorktreesForRepositoryInternal(path = repository.path)
            }
            if (selectedWorktreePath != null) {
                val restoredPath = selectedWorktreePath
                val hasRestoredPath =
                    repositories.any { repository ->
                        worktreesByRepositoryPath[repository.path].orEmpty().any { it.path == restoredPath }
                    }
                if (!hasRestoredPath) {
                    selectedWorktreePath = null
                }
            }
            persistSelection()
        }
    }
}

internal fun AppStoreCore.refreshInstalledEditors() {
    installedEditorIds.clear()
    allEditors.forEach { editor ->
        if (graph.editorOpening.isInstalled(editor = editor)) {
            installedEditorIds += editor.id
        }
    }
}

internal fun AppStoreCore.destroy() {
    scope.cancel()
}
