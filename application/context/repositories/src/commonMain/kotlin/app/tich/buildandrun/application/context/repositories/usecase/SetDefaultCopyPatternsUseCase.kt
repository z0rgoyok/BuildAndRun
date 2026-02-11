package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.domain.context.copy.model.CopyPattern

class SetDefaultCopyPatternsUseCase(
    private val preferencesStore: PreferencesStore,
) {
    fun execute(input: Input): UseCaseResult<Output> {
        return runCatchingCancellable {
            val patterns =
                input.patterns
                    .map(String::trim)
                    .filter(String::isNotBlank)
                    .distinct()
                    .map { pattern -> CopyPattern(pattern = pattern) }
            preferencesStore.defaultCopyPatterns = patterns
            UseCaseResult.Success(value = Output(patterns = patterns))
        }.fold(
            onSuccess = { it },
            onFailure = { throwable -> throwable.toUseCaseFailure() },
        )
    }

    data class Input(
        val patterns: List<String>,
    )

    data class Output(
        val patterns: List<CopyPattern>,
    )
}
