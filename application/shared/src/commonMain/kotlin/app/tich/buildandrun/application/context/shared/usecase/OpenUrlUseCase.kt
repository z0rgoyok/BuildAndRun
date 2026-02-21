package app.tich.buildandrun.application.context.shared.usecase

import app.tich.buildandrun.application.context.shared.port.SystemOpening

class OpenUrlUseCase(
    private val systemOpening: SystemOpening,
) {
    fun execute(input: Input): UseCaseResult<Output> {
        val url = input.url.trim()
        if (url.isBlank()) {
            return UseCaseResult.Success(value = Output)
        }
        return runCatchingCancellable {
            systemOpening.openURL(url = url)
            Output
        }.toUseCaseResult()
    }

    data class Input(
        val url: String,
    )

    data object Output
}
