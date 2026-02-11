package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.path.normalizePath
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.domain.context.editors.model.Editor
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId
import app.tich.buildandrun.domain.shared.error.AppError

class OpenInEditorUseCase(
    private val preferencesStore: PreferencesStore,
    private val editorOpening: EditorOpening,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val worktreePath = normalizePath(input.worktreePath)
        if (worktreePath.isBlank()) {
            return UseCaseResult.Success(value = Output(preferredEditorId = null))
        }

        val editor =
            resolveEditor(input = input)
                ?: return AppError.NoEditorConfigured().toUseCaseFailure()

        return runCatchingCancellable {
            editorOpening.open(path = worktreePath, withEditor = editor)
            if (input.rememberEditorChoice && input.repositoryId != null) {
                preferencesStore.setPreferredEditorId(
                    editorId = editor.id,
                    forRepositoryId = RepositoryId(input.repositoryId),
                )
                UseCaseResult.Success(value = Output(preferredEditorId = editor.id))
            } else {
                UseCaseResult.Success(value = Output(preferredEditorId = null))
            }
        }.fold(
            onSuccess = { it },
            onFailure = { throwable -> throwable.toUseCaseFailure() },
        )
    }

    private fun resolveEditor(input: Input): Editor? {
        val availableById = input.availableEditors.associateBy { it.id }
        val configuredEditorIds = input.enabledInstalledEditorIds.toSet()
        val explicitEditorId = input.editorId?.trim().orEmpty()
        if (explicitEditorId.isNotBlank() && configuredEditorIds.contains(explicitEditorId)) {
            return availableById[explicitEditorId]
        }
        if (input.rememberEditorChoice && input.repositoryId != null) {
            val preferredEditorId = preferencesStore.preferredEditorId(forRepositoryId = RepositoryId(input.repositoryId))
            if (preferredEditorId != null && configuredEditorIds.contains(preferredEditorId)) {
                return availableById[preferredEditorId]
            }
        }
        return input.enabledInstalledEditorIds.firstOrNull()?.let { firstId -> availableById[firstId] }
    }

    data class Input(
        val worktreePath: String,
        val editorId: String?,
        val repositoryId: String?,
        val rememberEditorChoice: Boolean,
        val enabledInstalledEditorIds: List<String>,
        val availableEditors: List<Editor>,
    )

    data class Output(
        val preferredEditorId: String?,
    )
}
