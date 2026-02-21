package app.tich.buildandrun.application.context.shared.usecase

import app.tich.buildandrun.application.context.shared.path.normalizePath
import app.tich.buildandrun.application.context.shared.port.SystemOpening

class OpenPathInTerminalUseCase(
    private val systemOpening: SystemOpening,
) {
    fun execute(input: Input): UseCaseResult<Output> {
        val normalizedPath = normalizePath(input.path)
        if (normalizedPath.isBlank()) {
            return UseCaseResult.Success(value = Output)
        }
        return runCatchingCancellable {
            systemOpening.openTerminal(atPath = normalizedPath)
            Output
        }.toUseCaseResult()
    }

    data class Input(
        val path: String,
    )

    data object Output
}
