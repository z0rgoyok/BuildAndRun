package app.tich.buildandrun.appstore

import app.tich.buildandrun.domain.entities.CopyPattern
import app.tich.buildandrun.domain.entities.KanbanTask
import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.entities.Worktree

internal fun AppRuntime.publishState() {
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

    val repositoryItems = buildRepositoryItems()
    mutableState.value =
        AppStore.State(
            isLoading = activityCenter.isGlobalActive,
            loadingMessage = activityCenter.currentGlobalMessage,
            repositories = repositoryItems,
            sidebarSections = buildSidebarSections(repositoryItems),
            expandedRepositoryIds = expandedRepositoryIds,
            collapsedGroupIds = collapsedGroupIds,
            repositoryGroups = buildRepositoryGroupItems(),
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
            error = error,
            success = success,
        )
}

internal fun AppRuntime.buildRepositoryItems(): List<AppStore.RepositoryItem> =
    repositories.map { repository ->
        AppStore.RepositoryItem(
            id = repository.id.value,
            name = repository.name,
            path = repository.path,
            isArchived = repository.isArchived,
            groupId = repository.groupId?.value,
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

internal fun AppRuntime.currentKanbanTasks(): List<AppStore.KanbanTaskItem> {
    selectedRepository() ?: return emptyList()
    val scopeKey = selectedScopeKey() ?: return emptyList()
    val tasks = tasksByScope[scopeKey].orEmpty()
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

internal fun AppRuntime.buildEditorItems(): List<AppStore.EditorItem> =
    allEditors.map { editor ->
        AppStore.EditorItem(
            id = editor.id,
            name = editor.name,
            icon = editor.icon,
            isInstalled = installedEditorIds.contains(editor.id),
            isEnabled = graph.preferencesStore.isEditorEnabled(editorId = editor.id),
        )
    }

internal fun AppRuntime.buildRemoteBranchItems(): List<AppStore.RemoteBranchItem> =
    hasRemoteBranchByWorktreePath.map { (worktreePath, hasRemote) ->
        AppStore.RemoteBranchItem(
            worktreePath = worktreePath,
            hasRemote = hasRemote,
        )
    }

internal fun AppRuntime.preferredEditorIdForSelectedRepository(): String? {
    val repository = selectedRepository() ?: return null
    return graph.preferencesStore.preferredEditorId(forRepositoryId = repository.id)
}

internal fun AppRuntime.selectedRepositoryCustomCopyPatterns(): List<String>? {
    val repository = selectedRepository() ?: return null
    val customPatterns = graph.preferencesStore.copyPatterns(forRepositoryId = repository.id) ?: return null
    return customPatterns.map(CopyPattern::pattern)
}

internal fun AppRuntime.selectedRepositoryEffectiveCopyPatterns(): List<String> {
    val repository = selectedRepository() ?: return defaultCopyPatterns.map(CopyPattern::pattern)
    return graph.preferencesStore.effectiveCopyPatterns(forRepositoryId = repository.id).map(CopyPattern::pattern)
}

internal fun AppRuntime.selectedRepository(): Repository? = repositories.firstOrNull { it.id.value == selectedRepositoryId }

internal fun AppRuntime.preferredSelectedRepository(): Repository? =
    repositories.firstOrNull { !it.isArchived } ?: repositories.firstOrNull()

internal fun AppRuntime.preferredSelectedRepositoryId(): String? = preferredSelectedRepository()?.id?.value

internal fun AppRuntime.selectedScopeKey(): String? {
    val repository = selectedRepository() ?: return null
    return repositoryScopeKey(repositoryId = repository.id.value)
}

internal fun AppRuntime.buildSidebarSections(repositoryItems: List<AppStore.RepositoryItem>): List<AppStore.SidebarSection> {
    val activeRepos = repositoryItems.filter { !it.isArchived }
    val ungrouped = activeRepos.filter { it.groupId == null }
    val groupedByGroupId = activeRepos.filter { it.groupId != null }.groupBy { it.groupId }
    val sortedGroups = repositoryGroups.sortedBy { it.sortOrder }

    val sections = mutableListOf<AppStore.SidebarSection>()
    if (ungrouped.isNotEmpty()) {
        sections +=
            AppStore.SidebarSection(
                groupId = null,
                groupName = null,
                repositories = ungrouped,
            )
    }
    for (group in sortedGroups) {
        val repos = groupedByGroupId[group.id.value].orEmpty()
        sections +=
            AppStore.SidebarSection(
                groupId = group.id.value,
                groupName = group.name,
                repositories = repos,
            )
    }
    return sections
}

internal fun AppRuntime.buildRepositoryGroupItems(): List<AppStore.RepositoryGroupItem> =
    repositoryGroups
        .sortedBy { it.sortOrder }
        .map { group ->
            AppStore.RepositoryGroupItem(
                id = group.id.value,
                name = group.name,
            )
        }
