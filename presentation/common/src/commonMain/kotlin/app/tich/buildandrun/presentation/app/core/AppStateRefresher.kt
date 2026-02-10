package app.tich.buildandrun.presentation.app.core

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.domain.context.copy.model.CopyPattern
import app.tich.buildandrun.domain.context.kanban.model.KanbanTask
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId
import app.tich.buildandrun.presentation.app.EditorItem
import app.tich.buildandrun.presentation.app.context.state.*

class AppStateRefresher(
    private val preferencesStore: PreferencesStore,
    private val activityState: ActivityContextState,
    val repositoriesState: RepositoriesContextState,
    val worktreesState: WorktreesContextState,
    val settingsState: SettingsContextState,
    val editorsState: EditorsContextState,
    val kanbanState: KanbanContextState,
    val messagesState: MessagesContextState,
) {
    fun isGlobalActive(): Boolean = activityState.activityCenter.isGlobalActive

    fun beginGlobalLoading(message: String): String = activityState.activityCenter.beginGlobal(message = message)

    fun beginWorktreeLoading(
        path: String,
        message: String,
    ): String = activityState.activityCenter.beginWorktree(path = path, message = message)

    fun endLoading(tokenId: String) {
        activityState.activityCenter.end(tokenId)
    }

    fun publishAll() {
        val selectedRepository =
            repositoriesState.selectedRepository()
                ?: repositoriesState.preferredSelectedRepository().also { repositoriesState.selectedRepositoryId = it?.id?.value }
        worktreesState.syncSelectionWithAvailableWorktrees(selectedRepository = selectedRepository)

        activityState.publish()
        repositoriesState.publish(worktreesState = worktreesState, activityCenter = activityState.activityCenter)
        worktreesState.publish()
        settingsState.selectedRepositoryCustomCopyPatterns = selectedRepositoryCustomCopyPatterns(selectedRepository)
        settingsState.selectedRepositoryEffectiveCopyPatterns = selectedRepositoryEffectiveCopyPatterns(selectedRepository)
        settingsState.publish()
        editorsState.preferredEditorId = preferredEditorIdForSelectedRepository(selectedRepository)
        editorsState.editorItems = buildEditorItems()
        editorsState.publish()
        kanbanState.publish(selectedRepositoryId = selectedRepository?.id?.value)
        messagesState.publish()
    }

    fun selectedRepository(): Repository? = repositoriesState.selectedRepository()

    fun preferredSelectedRepositoryId(): String? = repositoriesState.preferredSelectedRepositoryId()

    fun currentRepositoryId(): String? = selectedRepository()?.id?.value

    fun persistSelection() {
        preferencesStore.lastSelectedRepositoryId = repositoriesState.selectedRepositoryId
        preferencesStore.lastSelectedWorktreePath = worktreesState.selectedWorktreePath
    }

    fun refreshInstalledEditors(editorOpening: EditorOpening) {
        editorsState.allEditors = editorOpening.allEditors()
        editorsState.installedEditorIds.clear()
        editorsState.allEditors.forEach { editor ->
            if (editorOpening.isInstalled(editor = editor)) {
                editorsState.installedEditorIds += editor.id
            }
        }
    }

    fun cleanupRepositoryData(repository: Repository) {
        val removedWorktreePaths = worktreesState.worktreesByRepositoryPath.remove(repository.path).orEmpty().map { it.path }
        kanbanState.tasksByScope.remove(repositoryScopeKey(repositoryId = repository.id.value))
        preferencesStore.removeKanbanTasks(forRepositoryId = repository.id)
        removedWorktreePaths.forEach { worktreesState.worktreeStatusByPath.remove(it) }
        removedWorktreePaths.forEach { worktreesState.hasRemoteBranchByWorktreePath.remove(it) }
        worktreesState.worktreeStatusLoadingPaths.removeAll(removedWorktreePaths.toSet())
    }

    fun persistKanbanTasksForRepository(
        repositoryId: String,
        tasks: List<KanbanTask>,
    ) {
        preferencesStore.setKanbanTasks(
            tasks = tasks,
            forRepositoryId = RepositoryId(value = repositoryId),
        )
    }

    private fun preferredEditorIdForSelectedRepository(selectedRepository: Repository?): String? {
        val repository = selectedRepository ?: return null
        return preferencesStore.preferredEditorId(forRepositoryId = repository.id)
    }

    private fun buildEditorItems(): List<EditorItem> =
        editorsState.allEditors.map { editor ->
            EditorItem(
                id = editor.id,
                name = editor.name,
                icon = editor.icon,
                isInstalled = editorsState.installedEditorIds.contains(editor.id),
                isEnabled = preferencesStore.isEditorEnabled(editorId = editor.id),
            )
        }

    private fun selectedRepositoryCustomCopyPatterns(selectedRepository: Repository?): List<String>? {
        val repository = selectedRepository ?: return null
        val customPatterns = preferencesStore.copyPatterns(forRepositoryId = repository.id) ?: return null
        return customPatterns.map(CopyPattern::pattern)
    }

    private fun selectedRepositoryEffectiveCopyPatterns(selectedRepository: Repository?): List<String> {
        val repository = selectedRepository ?: return settingsState.defaultCopyPatterns.map(CopyPattern::pattern)
        return preferencesStore.effectiveCopyPatterns(forRepositoryId = repository.id).map(CopyPattern::pattern)
    }
}
