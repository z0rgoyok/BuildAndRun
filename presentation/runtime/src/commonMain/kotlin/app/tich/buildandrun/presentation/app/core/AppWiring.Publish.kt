package app.tich.buildandrun.presentation.app.core

import app.tich.buildandrun.domain.context.copy.model.CopyPattern
import app.tich.buildandrun.domain.context.kanban.model.KanbanTask
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.context.worktrees.model.Worktree
import app.tich.buildandrun.presentation.app.*

fun AppWiring.publishState() {
    val preferredSelection = preferredSelectedRepository()
    val selectedRepository =
        repositoriesState.repositories.firstOrNull { it.id.value == repositoriesState.selectedRepositoryId }
            ?: preferredSelection.also { repositoriesState.selectedRepositoryId = it?.id?.value }
    val selectedRepositoryPath = selectedRepository?.path.orEmpty()
    val availableWorktrees = worktreesState.worktreesByRepositoryPath[selectedRepositoryPath].orEmpty()
    if (worktreesState.selectedWorktreePath != null && availableWorktrees.none { it.path == worktreesState.selectedWorktreePath }) {
        worktreesState.selectedWorktreePath = null
    }
    worktreesState.createWorktreeState =
        worktreesState.createWorktreeState.copy(
            repositoryPath = selectedRepositoryPath,
        )

    val repositoryItems = buildRepositoryItems()

    mutableActivityState.value =
        ActivityState(
            isLoading = activityCenter.isGlobalActive,
            loadingMessage = activityCenter.currentGlobalMessage,
        )

    mutableRepositoriesState.value =
        RepositoriesState(
            repositories = repositoryItems,
            sidebarSections = buildSidebarSections(repositoryItems),
            expandedRepositoryIds = repositoriesState.expandedRepositoryIds,
            collapsedGroupIds = repositoriesState.collapsedGroupIds,
            repositoryGroups = buildRepositoryGroupItems(),
            selectedRepositoryId = repositoriesState.selectedRepositoryId,
            addRepositoryPathInput = repositoriesState.addRepositoryPathInput,
        )

    mutableWorktreesState.value =
        WorktreesState(
            selectedWorktreePath = worktreesState.selectedWorktreePath,
            remoteBranches = buildRemoteBranchItems(),
            createWorktree = worktreesState.createWorktreeState,
        )

    mutableSettingsState.value =
        SettingsState(
            branches = settingsState.branches,
            worktreeBasePath = settingsState.worktreeBasePath,
            defaultCopyPatterns = settingsState.defaultCopyPatterns.map(CopyPattern::pattern),
            selectedRepositoryCustomCopyPatterns = selectedRepositoryCustomCopyPatterns(),
            selectedRepositoryEffectiveCopyPatterns = selectedRepositoryEffectiveCopyPatterns(),
        )

    mutableEditorsState.value =
        EditorsState(
            rememberEditorChoice = editorsState.rememberEditorChoice,
            preferredEditorId = preferredEditorIdForSelectedRepository(),
            editors = buildEditorItems(),
        )

    mutableKanbanState.value = KanbanState(kanbanTasks = currentKanbanTasks())
    mutableMessagesState.value = MessagesState(error = messagesState.error, success = messagesState.success)
}

fun AppWiring.buildRepositoryItems(): List<RepositoryItem> =
    repositoriesState.repositories.map { repository ->
        RepositoryItem(
            id = repository.id.value,
            name = repository.name,
            path = repository.path,
            isArchived = repository.isArchived,
            groupId = repository.groupId?.value,
            worktrees =
                worktreesState.worktreesByRepositoryPath[repository.path]
                    .orEmpty()
                    .sortedWith(
                        compareByDescending<Worktree> { it.isMain }
                            .thenBy { it.name.lowercase() },
                    ).map {
                        WorktreeItem(
                            path = it.path,
                            name = it.name,
                            branch = it.branch,
                            baseBranch = it.baseBranch,
                            isMain = it.isMain,
                            isLocked = it.isLocked,
                            isPrunable = it.isPrunable,
                            status = worktreesState.worktreeStatusByPath[it.path],
                            isStatusLoading =
                                worktreesState.worktreeStatusLoadingPaths.contains(it.path) ||
                                    activityCenter.isWorktreeActive(it.path),
                        )
                    },
        )
    }

fun AppWiring.currentKanbanTasks(): List<KanbanTaskItem> {
    selectedRepository() ?: return emptyList()
    val scopeKey = selectedScopeKey() ?: return emptyList()
    val tasks = kanbanState.tasksByScope[scopeKey].orEmpty()
    return tasks
        .sortedWith(compareBy<KanbanTask> { it.columnId.ordinal }.thenBy { it.order })
        .map {
            KanbanTaskItem(
                id = it.id.value,
                title = it.title,
                description = it.description,
                columnId = it.columnId,
                order = it.order,
            )
        }
}

fun AppWiring.buildEditorItems(): List<EditorItem> =
    editorsState.allEditors.map { editor ->
        EditorItem(
            id = editor.id,
            name = editor.name,
            icon = editor.icon,
            isInstalled = editorsState.installedEditorIds.contains(editor.id),
            isEnabled = graph.preferencesStore.isEditorEnabled(editorId = editor.id),
        )
    }

fun AppWiring.buildRemoteBranchItems(): List<RemoteBranchItem> =
    worktreesState.hasRemoteBranchByWorktreePath.map { (worktreePath, hasRemote) ->
        RemoteBranchItem(
            worktreePath = worktreePath,
            hasRemote = hasRemote,
        )
    }

fun AppWiring.preferredEditorIdForSelectedRepository(): String? {
    val repository = selectedRepository() ?: return null
    return graph.preferencesStore.preferredEditorId(forRepositoryId = repository.id)
}

fun AppWiring.selectedRepositoryCustomCopyPatterns(): List<String>? {
    val repository = selectedRepository() ?: return null
    val customPatterns = graph.preferencesStore.copyPatterns(forRepositoryId = repository.id) ?: return null
    return customPatterns.map(CopyPattern::pattern)
}

fun AppWiring.selectedRepositoryEffectiveCopyPatterns(): List<String> {
    val repository = selectedRepository() ?: return settingsState.defaultCopyPatterns.map(CopyPattern::pattern)
    return graph.preferencesStore.effectiveCopyPatterns(forRepositoryId = repository.id).map(CopyPattern::pattern)
}

fun AppWiring.selectedRepository(): Repository? =
    repositoriesState.repositories.firstOrNull {
        it.id.value == repositoriesState.selectedRepositoryId
    }

fun AppWiring.preferredSelectedRepository(): Repository? =
    repositoriesState.repositories.firstOrNull { !it.isArchived } ?: repositoriesState.repositories.firstOrNull()

fun AppWiring.preferredSelectedRepositoryId(): String? = preferredSelectedRepository()?.id?.value

fun AppWiring.selectedScopeKey(): String? {
    val repository = selectedRepository() ?: return null
    return repositoryScopeKey(repositoryId = repository.id.value)
}

fun AppWiring.buildSidebarSections(repositoryItems: List<RepositoryItem>): List<SidebarSection> {
    val activeRepos = repositoryItems.filter { !it.isArchived }
    val ungrouped = activeRepos.filter { it.groupId == null }
    val groupedByGroupId = activeRepos.filter { it.groupId != null }.groupBy { it.groupId }
    val sortedGroups = repositoriesState.repositoryGroups.sortedBy { it.sortOrder }

    val sections = mutableListOf<SidebarSection>()
    if (ungrouped.isNotEmpty()) {
        sections +=
            SidebarSection(
                groupId = null,
                groupName = null,
                repositories = ungrouped,
            )
    }
    for (group in sortedGroups) {
        val repos = groupedByGroupId[group.id.value].orEmpty()
        sections +=
            SidebarSection(
                groupId = group.id.value,
                groupName = group.name,
                repositories = repos,
            )
    }
    return sections
}

fun AppWiring.buildRepositoryGroupItems(): List<RepositoryGroupItem> =
    repositoriesState.repositoryGroups
        .sortedBy { it.sortOrder }
        .map { group ->
            RepositoryGroupItem(
                id = group.id.value,
                name = group.name,
            )
        }
