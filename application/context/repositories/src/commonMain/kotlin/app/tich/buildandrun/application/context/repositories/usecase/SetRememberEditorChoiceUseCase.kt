package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId

class SetRememberEditorChoiceUseCase(
    private val preferencesStore: PreferencesStore,
) {
    fun execute(input: Input): UseCaseResult<Output> {
        return runCatchingCancellable {
            preferencesStore.rememberEditorChoice = input.value
            if (!input.value && input.repositoryId != null) {
                preferencesStore.removePreferredEditorId(forRepositoryId = RepositoryId(input.repositoryId))
            }
            UseCaseResult.Success(value = Output(rememberEditorChoice = input.value))
        }.fold(
            onSuccess = { it },
            onFailure = { throwable -> throwable.toUseCaseFailure() },
        )
    }

    data class Input(
        val value: Boolean,
        val repositoryId: String?,
    )

    data class Output(
        val rememberEditorChoice: Boolean,
    )
}
