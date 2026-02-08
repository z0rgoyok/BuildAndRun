package app.tich.buildandrun.application.usecases

import app.tich.buildandrun.application.ports.GitClient
import app.tich.buildandrun.domain.failures.DomainFailure
import app.tich.buildandrun.domain.failures.DomainFailureCode
import app.tich.buildandrun.domain.failures.DomainFailureMapper

class LoadBranchesUseCase(
    private val gitClient: GitClient,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val repositoryPath = input.repositoryPath.trim()
        if (repositoryPath.isBlank()) {
            return UseCaseResult.Failure(
                value =
                    DomainFailure.Validation(
                        code = DomainFailureCode.APP_VALIDATION_REPOSITORY_PATH_BLANK,
                        args = emptyList(),
                    ),
            )
        }
        return runCatching {
            val branches = gitClient.listBranches(atRepoPath = repositoryPath)
            UseCaseResult.Success(
                value = Output(branches = branches),
            )
        }.fold(
            onSuccess = { it },
            onFailure = { throwable ->
                UseCaseResult.Failure(
                    value = DomainFailureMapper.fromThrowable(throwable),
                )
            },
        )
    }

    data class Input(
        val repositoryPath: String,
    )

    data class Output(
        val branches: List<String>,
    )
}
