package app.tich.buildandrun.presentation.app.context.editors.impl

import app.tich.buildandrun.domain.context.editors.model.Editor
import app.tich.buildandrun.domain.shared.error.AppError
import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper
import app.tich.buildandrun.presentation.app.core.*
import kotlinx.coroutines.launch

internal fun AppRuntime.onSetRememberEditorChoice(value: Boolean) {
    editorsState.rememberEditorChoice = value
    graph.preferencesStore.rememberEditorChoice = value
    if (!value) {
        currentRepositoryId()?.let { repositoryId ->
            graph.preferencesStore.removePreferredEditorId(
                forRepositoryId = app.tich.buildandrun.domain.context.repositories.model.RepositoryId(repositoryId),
            )
        }
    }
    publishState()
}

internal fun AppRuntime.onSetEditorEnabled(
    editorId: String,
    enabled: Boolean,
) {
    val normalizedEditorId = editorId.trim()
    if (normalizedEditorId.isBlank()) {
        return
    }
    graph.preferencesStore.setEditorEnabled(
        editorId = normalizedEditorId,
        enabled = enabled,
        allEditorIds = editorsState.allEditors.map(Editor::id),
    )
    editorsState.enabledEditorIds = graph.preferencesStore.enabledEditorIds
    publishState()
}

internal fun AppRuntime.onSetPreferredEditor(editorId: String?) {
    val repository = selectedRepository() ?: return
    val normalizedEditorId = editorId?.trim().orEmpty()
    if (normalizedEditorId.isBlank()) {
        graph.preferencesStore.removePreferredEditorId(forRepositoryId = repository.id)
        publishState()
        return
    }
    graph.preferencesStore.setPreferredEditorId(
        editorId = normalizedEditorId,
        forRepositoryId = repository.id,
    )
    publishState()
}

internal fun AppRuntime.onOpenInEditor(
    worktreePath: String,
    editorId: String?,
) {
    val normalizedWorktreePath = normalizePath(worktreePath)
    if (normalizedWorktreePath.isBlank()) {
        return
    }
    val repository = selectedRepository()
    scope.launch {
        val editor = resolveEditor(editorId = editorId, repositoryId = repository?.id?.value)
        if (editor == null) {
            messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(AppError.NoEditorConfigured()))
            publishState()
            return@launch
        }
        runCatching {
            graph.editorOpening.open(
                path = normalizedWorktreePath,
                withEditor = editor,
            )
        }.onSuccess {
            if (editorsState.rememberEditorChoice && repository != null) {
                graph.preferencesStore.setPreferredEditorId(
                    editorId = editor.id,
                    forRepositoryId = repository.id,
                )
            }
            clearMessages()
        }.onFailure { throwable ->
            messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        publishState()
    }
}

internal fun AppRuntime.onOpenInFinder(worktreePath: String) {
    val normalizedWorktreePath = normalizePath(worktreePath)
    if (normalizedWorktreePath.isBlank()) {
        return
    }
    graph.systemOpening.revealInFinder(path = normalizedWorktreePath)
}

internal fun AppRuntime.onOpenInTerminal(worktreePath: String) {
    val normalizedWorktreePath = normalizePath(worktreePath)
    if (normalizedWorktreePath.isBlank()) {
        return
    }
    graph.systemOpening.openTerminal(atPath = normalizedWorktreePath)
}

private fun AppRuntime.resolveEditor(
    editorId: String?,
    repositoryId: String?,
): Editor? {
    val configuredEditors = buildEditorItems().filter { it.isEnabled && it.isInstalled }
    val configuredIds = configuredEditors.map { it.id }.toSet()
    val explicitEditorId = editorId?.trim().orEmpty()
    if (explicitEditorId.isNotBlank() && configuredIds.contains(explicitEditorId)) {
        return editorsState.allEditors.firstOrNull { it.id == explicitEditorId }
    }
    if (editorsState.rememberEditorChoice && repositoryId != null) {
        val preferredEditorId =
            graph.preferencesStore.preferredEditorId(
                forRepositoryId = app.tich.buildandrun.domain.context.repositories.model.RepositoryId(repositoryId),
            )
        if (preferredEditorId != null && configuredIds.contains(preferredEditorId)) {
            return editorsState.allEditors.firstOrNull { it.id == preferredEditorId }
        }
    }
    return configuredEditors.firstOrNull()?.let { first -> editorsState.allEditors.firstOrNull { it.id == first.id } }
}
