package app.tich.buildandrun.appstore

import app.tich.buildandrun.domain.entities.CopyPattern
import app.tich.buildandrun.domain.entities.KanbanTask
import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.entities.Worktree

internal fun AppStoreCore.publishState() {
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
        AppStore.State(
            isLoading = activityCenter.isGlobalActive,
            loadingMessage = activityCenter.currentGlobalMessage,
            repositories = buildRepositoryItems(),
            selectedRepositoryId = selectedRepositoryId,
            selectedWorktreePath = selectedWorktreePath,
            addRepositoryPathInput = addRepositoryPathInput,
            branches = branches,
            worktreeBasePath = worktreeBasePath,
            defaultCopyPatterns = defaultCopyPatterns.map(CopyPattern::pattern),
            selectedRepositoryCustomCopyPatterns = selectedRepositoryCustomCopyPatterns(),
            selectedRepositoryEffectiveCopyPatterns = selectedRepositoryEffectiveCopyPatterns(),
            rememberEditorChoice = rememberEditorChoice,
            preferredEditorId = preferredEditorIdForSelectedRepository(),
            editors = buildEditorItems(),
            remoteBranches = buildRemoteBranchItems(),
            createWorktree = createWorktreeState,
            kanbanTasks = currentKanbanTasks(),
            activeChild = activeChild,
            activeSheet = activeSheet,
            error = error,
            success = success,
        )
}

internal fun AppStoreCore.buildRepositoryItems(): List<AppStore.RepositoryItem> =
    repositories.map { repository ->
        AppStore.RepositoryItem(
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
                        AppStore.WorktreeItem(
                            path = it.path,
                            name = it.name,
                            branch = it.branch,
                            baseBranch = it.baseBranch,
                            isMain = it.isMain,
                            isLocked = it.isLocked,
                            isPrunable = it.isPrunable,
                            status = worktreeStatusByPath[it.path],
                            isStatusLoading = worktreeStatusLoadingPaths.contains(it.path) || activityCenter.isWorktreeActive(it.path),
                        )
                    },
        )
    }

internal fun AppStoreCore.currentKanbanTasks(): List<AppStore.KanbanTaskItem> {
    val scopeKey = selectedScopeKey() ?: return emptyList()
    val tasks = tasksByScope.getOrPut(scopeKey) { createDefaultTasks(currentWorktreePath()) }
    return tasks
        .sortedWith(compareBy<KanbanTask> { it.columnId.ordinal }.thenBy { it.order })
        .map {
            AppStore.KanbanTaskItem(
                id = it.id.value,
                title = it.title,
                description = it.description,
                columnId = it.columnId,
                order = it.order,
            )
        }
}

internal fun AppStoreCore.buildEditorItems(): List<AppStore.EditorItem> =
    allEditors.map { editor ->
        AppStore.EditorItem(
            id = editor.id,
            name = editor.name,
            icon = editor.icon,
            isInstalled = installedEditorIds.contains(editor.id),
            isEnabled = graph.preferencesStore.isEditorEnabled(editorId = editor.id),
        )
    }

internal fun AppStoreCore.buildRemoteBranchItems(): List<AppStore.RemoteBranchItem> =
    hasRemoteBranchByWorktreePath.map { (worktreePath, hasRemote) ->
        AppStore.RemoteBranchItem(
            worktreePath = worktreePath,
            hasRemote = hasRemote,
        )
    }

internal fun AppStoreCore.preferredEditorIdForSelectedRepository(): String? {
    val repository = selectedRepository() ?: return null
    return graph.preferencesStore.preferredEditorId(forRepositoryId = repository.id)
}

internal fun AppStoreCore.selectedRepositoryCustomCopyPatterns(): List<String>? {
    val repository = selectedRepository() ?: return null
    val customPatterns = graph.preferencesStore.copyPatterns(forRepositoryId = repository.id) ?: return null
    return customPatterns.map(CopyPattern::pattern)
}

internal fun AppStoreCore.selectedRepositoryEffectiveCopyPatterns(): List<String> {
    val repository = selectedRepository() ?: return defaultCopyPatterns.map(CopyPattern::pattern)
    return graph.preferencesStore.effectiveCopyPatterns(forRepositoryId = repository.id).map(CopyPattern::pattern)
}

internal fun AppStoreCore.selectedRepository(): Repository? = repositories.firstOrNull { it.id.value == selectedRepositoryId }

internal fun AppStoreCore.preferredSelectedRepository(): Repository? =
    repositories.firstOrNull { !it.isArchived } ?: repositories.firstOrNull()

internal fun AppStoreCore.preferredSelectedRepositoryId(): String? = preferredSelectedRepository()?.id?.value

internal fun AppStoreCore.selectedScopeKey(): String? {
    val repository = selectedRepository() ?: return null
    return selectedWorktreePath?.let { "worktree:$it" } ?: "repo:${repository.id.value}"
}

internal fun AppStoreCore.currentWorktreePath(): String? = selectedWorktreePath
