package app.tich.buildandrun.presentation.app.core

import app.tich.buildandrun.application.context.repositories.usecase.LoadPresentationPreferencesUseCase
import app.tich.buildandrun.application.context.shared.usecase.LoadInstalledEditorsUseCase
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.copy.model.CopyPattern
import app.tich.buildandrun.domain.context.editors.model.Editor
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.presentation.app.EditorItem
import app.tich.buildandrun.presentation.app.context.state.*

class AppStateRefresher(
    private val activityState: ActivityContextState,
    private val errorMapper: AppErrorStateMapper,
    private val loadPresentationPreferencesUseCase: LoadPresentationPreferencesUseCase,
    private val loadInstalledEditorsUseCase: LoadInstalledEditorsUseCase,
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

        val preferencesProjection = resolvePresentationPreferences(selectedRepository = selectedRepository)

        activityState.publish()
        repositoriesState.publish(worktreesState = worktreesState, activityCenter = activityState.activityCenter)
        worktreesState.publish()
        settingsState.selectedRepositoryCustomCopyPatterns = preferencesProjection.selectedRepositoryCustomCopyPatterns
        settingsState.selectedRepositoryEffectiveCopyPatterns = preferencesProjection.selectedRepositoryEffectiveCopyPatterns
        settingsState.publish()
        editorsState.preferredEditorId = preferencesProjection.preferredEditorId
        editorsState.editorItems = buildEditorItems(enabledEditorIds = preferencesProjection.enabledEditorIds)
        editorsState.publish()
        kanbanState.publish(selectedRepositoryId = selectedRepository?.id?.value)
        messagesState.publish()
    }

    fun selectedRepository(): Repository? = repositoriesState.selectedRepository()

    fun preferredSelectedRepositoryId(): String? = repositoriesState.preferredSelectedRepositoryId()

    fun currentRepositoryId(): String? = selectedRepository()?.id?.value

    fun refreshInstalledEditors() {
        when (val result = loadInstalledEditorsUseCase.execute()) {
            is UseCaseResult.Success -> {
                editorsState.allEditors = result.value.allEditors
                editorsState.installedEditorIds.clear()
                editorsState.installedEditorIds.addAll(result.value.installedEditorIds)
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
    }

    private fun resolvePresentationPreferences(selectedRepository: Repository?): PresentationPreferences {
        val selectedRepositoryId = selectedRepository?.id?.value
        val editorIds = editorsState.allEditors.map(Editor::id)
        return when (
            val result =
                loadPresentationPreferencesUseCase.execute(
                    input =
                        LoadPresentationPreferencesUseCase.Input(
                            repositoryId = selectedRepositoryId,
                            editorIds = editorIds,
                            defaultCopyPatterns = settingsState.defaultCopyPatterns,
                        ),
                )
        ) {
            is UseCaseResult.Success -> {
                PresentationPreferences(
                    preferredEditorId = result.value.preferredEditorId,
                    enabledEditorIds = result.value.enabledEditorIds,
                    selectedRepositoryCustomCopyPatterns = result.value.selectedRepositoryCustomCopyPatterns,
                    selectedRepositoryEffectiveCopyPatterns = result.value.selectedRepositoryEffectiveCopyPatterns,
                )
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                PresentationPreferences(
                    preferredEditorId = editorsState.preferredEditorId,
                    enabledEditorIds = editorIds.toSet(),
                    selectedRepositoryCustomCopyPatterns = settingsState.selectedRepositoryCustomCopyPatterns,
                    selectedRepositoryEffectiveCopyPatterns =
                        if (selectedRepositoryId == null) {
                            settingsState.defaultCopyPatterns.map(CopyPattern::pattern)
                        } else {
                            settingsState.selectedRepositoryEffectiveCopyPatterns
                        },
                )
            }
        }
    }

    private fun buildEditorItems(enabledEditorIds: Set<String>): List<EditorItem> =
        editorsState.allEditors.map { editor ->
            EditorItem(
                id = editor.id,
                name = editor.name,
                icon = editor.icon,
                isInstalled = editorsState.installedEditorIds.contains(editor.id),
                isEnabled = enabledEditorIds.contains(editor.id),
            )
        }

    private data class PresentationPreferences(
        val preferredEditorId: String?,
        val enabledEditorIds: Set<String>,
        val selectedRepositoryCustomCopyPatterns: List<String>?,
        val selectedRepositoryEffectiveCopyPatterns: List<String>,
    )
}
