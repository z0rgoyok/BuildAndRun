package app.tich.buildandrun.appstore

import app.tich.buildandrun.domain.entities.Editor
import app.tich.buildandrun.domain.errors.AppError
import app.tich.buildandrun.domain.failures.DomainFailureMapper
import kotlinx.coroutines.launch

internal fun AppStoreCore.onSetRememberEditorChoice(value: Boolean) {
    rememberEditorChoice = value
    graph.preferencesStore.rememberEditorChoice = value
    if (!value) {
        currentRepositoryId()?.let { repositoryId ->
            graph.preferencesStore.removePreferredEditorId(
                forRepositoryId = app.tich.buildandrun.domain.entities.RepositoryId(repositoryId),
            )
        }
    }
    publishState()
}

internal fun AppStoreCore.onSetEditorEnabled(
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
        allEditorIds = allEditors.map(Editor::id),
    )
    enabledEditorIds = graph.preferencesStore.enabledEditorIds
    publishState()
}

internal fun AppStoreCore.onSetPreferredEditor(editorId: String?) {
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

internal fun AppStoreCore.onOpenInEditor(
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
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(AppError.NoEditorConfigured()))
            publishState()
            return@launch
        }
        runCatching {
            graph.editorOpening.open(
                path = normalizedWorktreePath,
                withEditor = editor,
            )
        }.onSuccess {
            if (rememberEditorChoice && repository != null) {
                graph.preferencesStore.setPreferredEditorId(
                    editorId = editor.id,
                    forRepositoryId = repository.id,
                )
            }
            clearMessages()
        }.onFailure { throwable ->
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        publishState()
    }
}

internal fun AppStoreCore.onOpenInFinder(worktreePath: String) {
    val normalizedWorktreePath = normalizePath(worktreePath)
    if (normalizedWorktreePath.isBlank()) {
        return
    }
    graph.systemOpening.revealInFinder(path = normalizedWorktreePath)
}

internal fun AppStoreCore.onOpenInTerminal(worktreePath: String) {
    val normalizedWorktreePath = normalizePath(worktreePath)
    if (normalizedWorktreePath.isBlank()) {
        return
    }
    graph.systemOpening.openTerminal(atPath = normalizedWorktreePath)
}

private fun AppStoreCore.resolveEditor(
    editorId: String?,
    repositoryId: String?,
): Editor? {
    val configuredEditors = buildEditorItems().filter { it.isEnabled && it.isInstalled }
    val configuredIds = configuredEditors.map { it.id }.toSet()
    val explicitEditorId = editorId?.trim().orEmpty()
    if (explicitEditorId.isNotBlank() && configuredIds.contains(explicitEditorId)) {
        return allEditors.firstOrNull { it.id == explicitEditorId }
    }
    if (rememberEditorChoice && repositoryId != null) {
        val preferredEditorId =
            graph.preferencesStore.preferredEditorId(
                forRepositoryId = app.tich.buildandrun.domain.entities.RepositoryId(repositoryId),
            )
        if (preferredEditorId != null && configuredIds.contains(preferredEditorId)) {
            return allEditors.firstOrNull { it.id == preferredEditorId }
        }
    }
    return configuredEditors.firstOrNull()?.let { first -> allEditors.firstOrNull { it.id == first.id } }
}
