package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.path.normalizePath
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.context.worktrees.model.Worktree
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

class AddRepositoryUseCase(
    private val gitClient: GitClient,
    private val preferencesStore: PreferencesStore,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val path = normalizePath(input.path)
        if (path.isBlank()) {
            return UseCaseResult.Failure(
                DomainFailure.Validation(
                    code = DomainFailureCode.APP_VALIDATION_REPOSITORY_PATH_BLANK,
                    args = emptyList(),
                ),
            )
        }

        return runCatchingCancellable {
            val repositoryRoot = normalizePath(gitClient.getRepositoryRoot(atPath = path))
            val existingRepositories = preferencesStore.loadRepositories()
            if (existingRepositories.any { normalizePath(it.path) == repositoryRoot }) {
                UseCaseResult.Failure(
                    DomainFailure.Conflict(
                        code = DomainFailureCode.APP_REPOSITORY_ALREADY_ADDED,
                        args = listOf(repositoryRoot),
                        isRetryable = false,
                    ),
                )
            } else {
                val repository = Repository.create(path = repositoryRoot)
                val repositories = (existingRepositories + repository).sortedBy { it.name.lowercase() }
                preferencesStore.saveRepositories(repositories = repositories)
                val worktrees = gitClient.listWorktrees(atRepoPath = repositoryRoot)
                UseCaseResult.Success(
                    Output(
                        repositories = repositories,
                        addedRepository = repository,
                        worktrees = worktrees,
                    ),
                )
            }
        }.fold(
            onSuccess = { it },
            onFailure = { throwable ->
                throwable.toUseCaseFailure()
            },
        )
    }

    data class Input(val path: String)

    data class Output(
        val repositories: List<Repository>,
        val addedRepository: Repository,
        val worktrees: List<Worktree>,
    )
}
