package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId

class SetPreferredEditorUseCase(
    private val preferencesStore: PreferencesStore,
) {
    fun execute(input: Input): UseCaseResult<Output> {
        return runCatchingCancellable {
            val editorId = input.editorId?.trim().orEmpty()
            if (editorId.isBlank()) {
                preferencesStore.removePreferredEditorId(forRepositoryId = RepositoryId(input.repositoryId))
                UseCaseResult.Success(value = Output(preferredEditorId = null))
            } else {
                preferencesStore.setPreferredEditorId(
                    editorId = editorId,
                    forRepositoryId = RepositoryId(input.repositoryId),
                )
                UseCaseResult.Success(value = Output(preferredEditorId = editorId))
            }
        }.fold(
            onSuccess = { it },
            onFailure = { throwable -> throwable.toUseCaseFailure() },
        )
    }

    data class Input(
        val repositoryId: String,
        val editorId: String?,
    )

    data class Output(
        val preferredEditorId: String?,
    )
}
