package app.tich.buildandrun.presentation.app.core

import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper
import app.tich.buildandrun.presentation.app.context.worktrees.impl.loadWorktreesForRepositoryInternal
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
                        repositoriesState.repositories = result.value
                        repositoriesState.repositoryGroups = graph.preferencesStore.loadRepositoryGroups()
                        repositoriesState.selectedRepositoryId =
                            graph.preferencesStore.lastSelectedRepositoryId
                                ?.takeIf { selectedId -> repositoriesState.repositories.any { it.id.value == selectedId } }
                                ?: preferredSelectedRepositoryId()
                        repositoriesState.repositories.forEach { repository ->
                            val persistedTasks = graph.preferencesStore.loadKanbanTasks(forRepositoryId = repository.id)
                            if (persistedTasks.isNotEmpty()) {
                                kanbanState.tasksByScope[repositoryScopeKey(repositoryId = repository.id.value)] = persistedTasks.toMutableList()
                            }
                        }
                        worktreesState.selectedWorktreePath = graph.preferencesStore.lastSelectedWorktreePath
                        settingsState.worktreeBasePath = graph.preferencesStore.worktreeBasePath
                        settingsState.defaultCopyPatterns = graph.preferencesStore.defaultCopyPatterns
                        editorsState.rememberEditorChoice = graph.preferencesStore.rememberEditorChoice
                        editorsState.enabledEditorIds = graph.preferencesStore.enabledEditorIds
                    }

                    is UseCaseResult.Failure -> {
                        messagesState.error = mapFailureToErrorState(result.value)
                    }
                }
                refreshInstalledEditors()
                repositoriesState.repositories.forEach { repository ->
                    loadWorktreesForRepositoryInternal(path = repository.path)
                }
                if (worktreesState.selectedWorktreePath != null) {
                    val restoredPath = worktreesState.selectedWorktreePath
                    val hasRestoredPath =
                        repositoriesState.repositories.any { repository ->
                            worktreesState.worktreesByRepositoryPath[repository.path].orEmpty().any { it.path == restoredPath }
                        }
                    if (!hasRestoredPath) {
                        worktreesState.selectedWorktreePath = null
                    }
                }
                persistSelection()
            }
        }.onFailure { throwable ->
            messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            publishState()
        }
    }
}

internal fun AppRuntime.refreshInstalledEditors() {
    editorsState.installedEditorIds.clear()
    editorsState.allEditors.forEach { editor ->
        if (graph.editorOpening.isInstalled(editor = editor)) {
            editorsState.installedEditorIds += editor.id
        }
    }
}

internal fun AppRuntime.destroy() {
    scope.cancel()
}
