package app.tich.buildandrun.presentation.app.context.editors.impl

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.port.SystemOpening
import app.tich.buildandrun.domain.context.editors.model.Editor
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId
import app.tich.buildandrun.domain.shared.error.AppError
import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper
import app.tich.buildandrun.presentation.app.AppEditorsFeature
import app.tich.buildandrun.presentation.app.context.state.EditorsContextState
import app.tich.buildandrun.presentation.app.context.state.MessagesContextState
import app.tich.buildandrun.presentation.app.context.state.RepositoriesContextState
import app.tich.buildandrun.presentation.app.core.AppErrorStateMapper
import app.tich.buildandrun.presentation.app.core.AppExecutionScope
import app.tich.buildandrun.presentation.app.core.AppStateRefresher
import app.tich.buildandrun.presentation.app.core.normalizePath
import kotlinx.coroutines.launch

class AppEditorsService(
    private val executionScope: AppExecutionScope,
    private val stateRefresher: AppStateRefresher,
    private val errorMapper: AppErrorStateMapper,
    private val repositoriesState: RepositoriesContextState,
    private val editorsState: EditorsContextState,
    private val messagesState: MessagesContextState,
    private val preferencesStore: PreferencesStore,
    private val editorOpening: EditorOpening,
    private val systemOpening: SystemOpening,
) : AppEditorsFeature {
    override fun onSetRememberEditorChoice(value: Boolean) {
        editorsState.rememberEditorChoice = value
        preferencesStore.rememberEditorChoice = value
        if (!value) {
            stateRefresher.currentRepositoryId()?.let { repositoryId ->
                preferencesStore.removePreferredEditorId(
                    forRepositoryId = RepositoryId(repositoryId),
                )
            }
        }
        stateRefresher.publishAll()
    }

    override fun onSetEditorEnabled(
        editorId: String,
        enabled: Boolean,
    ) {
        val normalizedEditorId = editorId.trim()
        if (normalizedEditorId.isBlank()) {
            return
        }
        preferencesStore.setEditorEnabled(
            editorId = normalizedEditorId,
            enabled = enabled,
            allEditorIds = editorsState.allEditors.map(Editor::id),
        )
        editorsState.enabledEditorIds = preferencesStore.enabledEditorIds
        stateRefresher.publishAll()
    }

    override fun onSetPreferredEditor(editorId: String?) {
        val repository = repositoriesState.selectedRepository() ?: return
        val normalizedEditorId = editorId?.trim().orEmpty()
        if (normalizedEditorId.isBlank()) {
            preferencesStore.removePreferredEditorId(forRepositoryId = repository.id)
            stateRefresher.publishAll()
            return
        }
        preferencesStore.setPreferredEditorId(
            editorId = normalizedEditorId,
            forRepositoryId = repository.id,
        )
        stateRefresher.publishAll()
    }

    override fun onOpenInEditor(
        worktreePath: String,
        editorId: String?,
    ) {
        val normalizedWorktreePath = normalizePath(worktreePath)
        if (normalizedWorktreePath.isBlank()) {
            return
        }
        val repository = repositoriesState.selectedRepository()
        executionScope.scope.launch {
            val editor = resolveEditor(editorId = editorId, repositoryId = repository?.id?.value)
            if (editor == null) {
                messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(AppError.NoEditorConfigured()))
                stateRefresher.publishAll()
                return@launch
            }
            runCatching {
                editorOpening.open(
                    path = normalizedWorktreePath,
                    withEditor = editor,
                )
            }.onSuccess {
                if (editorsState.rememberEditorChoice && repository != null) {
                    preferencesStore.setPreferredEditorId(
                        editorId = editor.id,
                        forRepositoryId = repository.id,
                    )
                }
                messagesState.clear()
            }.onFailure { throwable ->
                messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            }
            stateRefresher.publishAll()
        }
    }

    override fun onOpenInFinder(worktreePath: String) {
        val normalizedWorktreePath = normalizePath(worktreePath)
        if (normalizedWorktreePath.isBlank()) {
            return
        }
        systemOpening.revealInFinder(path = normalizedWorktreePath)
    }

    override fun onOpenInTerminal(worktreePath: String) {
        val normalizedWorktreePath = normalizePath(worktreePath)
        if (normalizedWorktreePath.isBlank()) {
            return
        }
        systemOpening.openTerminal(atPath = normalizedWorktreePath)
    }

    private fun resolveEditor(
        editorId: String?,
        repositoryId: String?,
    ): Editor? {
        val configuredEditors = editorsState.editorItems.filter { it.isEnabled && it.isInstalled }
        val configuredIds = configuredEditors.map { it.id }.toSet()
        val explicitEditorId = editorId?.trim().orEmpty()
        if (explicitEditorId.isNotBlank() && configuredIds.contains(explicitEditorId)) {
            return editorsState.allEditors.firstOrNull { it.id == explicitEditorId }
        }
        if (editorsState.rememberEditorChoice && repositoryId != null) {
            val preferredEditorId =
                preferencesStore.preferredEditorId(
                    forRepositoryId = RepositoryId(repositoryId),
                )
            if (preferredEditorId != null && configuredIds.contains(preferredEditorId)) {
                return editorsState.allEditors.firstOrNull { it.id == preferredEditorId }
            }
        }
        return configuredEditors.firstOrNull()?.let { first -> editorsState.allEditors.firstOrNull { it.id == first.id } }
    }
}
