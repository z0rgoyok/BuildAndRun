package app.tich.buildandrun.appstore

import app.tich.buildandrun.application.usecases.UseCaseResult
import app.tich.buildandrun.domain.failures.DomainFailureMapper
import app.tich.buildandrun.resources.Res
import app.tich.buildandrun.resources.loading_initial
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal fun AppRuntime.loadInitial() {
    scope.launch {
        runCatching {
            withGlobalLoading(Res.string.loading_initial) {
                when (val result = graph.loadRepositoriesUseCase.execute()) {
                    is UseCaseResult.Success -> {
                        repositories = result.value
                        repositoryGroups = graph.preferencesStore.loadRepositoryGroups()
                        selectedRepositoryId =
                            graph.preferencesStore.lastSelectedRepositoryId
                                ?.takeIf { selectedId -> repositories.any { it.id.value == selectedId } }
                                ?: preferredSelectedRepositoryId()
                        repositories.forEach { repository ->
                            val persistedTasks = graph.preferencesStore.loadKanbanTasks(forRepositoryId = repository.id)
                            if (persistedTasks.isNotEmpty()) {
                                tasksByScope[repositoryScopeKey(repositoryId = repository.id.value)] = persistedTasks.toMutableList()
                            }
                        }
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
        }.onFailure { throwable ->
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            publishState()
        }
    }
}

internal fun AppRuntime.refreshInstalledEditors() {
    installedEditorIds.clear()
    allEditors.forEach { editor ->
        if (graph.editorOpening.isInstalled(editor = editor)) {
            installedEditorIds += editor.id
        }
    }
}

internal fun AppRuntime.destroy() {
    scope.cancel()
}
