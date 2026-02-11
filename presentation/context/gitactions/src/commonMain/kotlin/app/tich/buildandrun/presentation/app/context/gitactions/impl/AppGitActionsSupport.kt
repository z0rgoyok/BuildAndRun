package app.tich.buildandrun.presentation.app.context.gitactions.impl

import app.tich.buildandrun.application.context.repositories.usecase.AppSessionPersistenceUseCase
import app.tich.buildandrun.application.context.shared.usecase.OpenUrlUseCase
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.shared.failure.DomainFailure

internal fun openGitActionUrl(
    openUrlUseCase: OpenUrlUseCase,
    url: String,
    onFailure: (DomainFailure) -> Unit,
): Boolean {
    return when (val result = openUrlUseCase.execute(input = OpenUrlUseCase.Input(url = url))) {
        is UseCaseResult.Success -> true
        is UseCaseResult.Failure -> {
            onFailure(result.value)
            false
        }
    }
}

internal fun persistGitSelection(
    appSessionPersistenceUseCase: AppSessionPersistenceUseCase,
    repositoryId: String?,
    worktreePath: String?,
    onFailure: (DomainFailure) -> Unit,
) {
    when (
        val result =
            appSessionPersistenceUseCase.execute(
                input =
                    AppSessionPersistenceUseCase.Input(
                        repositoryId = repositoryId,
                        worktreePath = worktreePath,
                    ),
            )
    ) {
        is UseCaseResult.Success -> Unit
        is UseCaseResult.Failure -> onFailure(result.value)
    }
}
