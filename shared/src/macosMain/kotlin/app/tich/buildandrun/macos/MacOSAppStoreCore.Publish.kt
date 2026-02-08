package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.entities.KanbanTask
import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.entities.Worktree

internal fun MacOSAppStoreCore.publishState() {
    val preferredSelection = preferredSelectedRepository()
    val selectedRepository =
        repositories.firstOrNull { it.id.value == selectedRepositoryId }
            ?: preferredSelection.also { selectedRepositoryId = it?.id?.value }
    val selectedRepositoryPath = selectedRepository?.path.orEmpty()
    val availableWorktrees = worktreesByRepositoryPath[selectedRepositoryPath].orEmpty()
    if (selectedWorktreePath != null && availableWorktrees.none { it.path == selectedWorktreePath }) {
        selectedWorktreePath = null
    }
    createWorktreeState =
        createWorktreeState.copy(
            repositoryPath = selectedRepositoryPath,
        )

    mutableState.value =
        MacOSAppStore.State(
            isLoading = isLoading,
            repositories = buildRepositoryItems(),
            selectedRepositoryId = selectedRepositoryId,
            selectedWorktreePath = selectedWorktreePath,
            addRepositoryPathInput = addRepositoryPathInput,
            createWorktree = createWorktreeState,
            kanbanTasks = currentKanbanTasks(),
            error = error,
            success = success,
        )
}

internal fun MacOSAppStoreCore.buildRepositoryItems(): List<MacOSAppStore.RepositoryItem> =
    repositories.map { repository ->
        MacOSAppStore.RepositoryItem(
            id = repository.id.value,
            name = repository.name,
            path = repository.path,
            isArchived = repository.isArchived,
            worktrees =
                worktreesByRepositoryPath[repository.path]
                    .orEmpty()
                    .sortedWith(
                        compareByDescending<Worktree> { it.isMain }
                            .thenBy { it.name.lowercase() },
                    ).map {
                        MacOSAppStore.WorktreeItem(
                            path = it.path,
                            name = it.name,
                            branch = it.branch,
                            isMain = it.isMain,
                            isLocked = it.isLocked,
                            isPrunable = it.isPrunable,
                            status = worktreeStatusByPath[it.path],
                            isStatusLoading = worktreeStatusLoadingPaths.contains(it.path),
                        )
                    },
        )
    }

internal fun MacOSAppStoreCore.currentKanbanTasks(): List<MacOSAppStore.KanbanTaskItem> {
    val scopeKey = selectedScopeKey() ?: return emptyList()
    val tasks = tasksByScope.getOrPut(scopeKey) { createDefaultTasks(currentWorktreePath()) }
    return tasks
        .sortedWith(compareBy<KanbanTask> { it.columnId.ordinal }.thenBy { it.order })
        .map {
            MacOSAppStore.KanbanTaskItem(
                id = it.id.value,
                title = it.title,
                description = it.description,
                columnId = it.columnId,
                order = it.order,
            )
        }
}

internal fun MacOSAppStoreCore.selectedRepository(): Repository? = repositories.firstOrNull { it.id.value == selectedRepositoryId }

internal fun MacOSAppStoreCore.preferredSelectedRepository(): Repository? =
    repositories.firstOrNull { !it.isArchived } ?: repositories.firstOrNull()

internal fun MacOSAppStoreCore.preferredSelectedRepositoryId(): String? = preferredSelectedRepository()?.id?.value

internal fun MacOSAppStoreCore.selectedScopeKey(): String? {
    val repository = selectedRepository() ?: return null
    return selectedWorktreePath?.let { "worktree:$it" } ?: "repo:${repository.id.value}"
}

internal fun MacOSAppStoreCore.currentWorktreePath(): String? = selectedWorktreePath
