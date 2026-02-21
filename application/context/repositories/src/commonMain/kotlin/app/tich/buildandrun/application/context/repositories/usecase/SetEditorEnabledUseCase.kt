package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure

class SetEditorEnabledUseCase(
    private val preferencesStore: PreferencesStore,
) {
    fun execute(input: Input): UseCaseResult<Output> {
        val editorId = input.editorId.trim()
        if (editorId.isBlank()) {
            return UseCaseResult.Success(value = Output(enabledEditorIds = input.currentEnabledEditorIds))
        }

        return runCatchingCancellable {
            preferencesStore.setEditorEnabled(
                editorId = editorId,
                enabled = input.enabled,
                allEditorIds = input.allEditorIds,
            )
            UseCaseResult.Success(value = Output(enabledEditorIds = preferencesStore.enabledEditorIds))
        }.fold(
            onSuccess = { it },
            onFailure = { throwable -> throwable.toUseCaseFailure() },
        )
    }

    data class Input(
        val editorId: String,
        val enabled: Boolean,
        val allEditorIds: List<String>,
        val currentEnabledEditorIds: Set<String>?,
    )

    data class Output(
        val enabledEditorIds: Set<String>?,
    )
}
