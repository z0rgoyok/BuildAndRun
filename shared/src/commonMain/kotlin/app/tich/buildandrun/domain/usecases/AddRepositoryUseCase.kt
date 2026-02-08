package app.tich.buildandrun.domain.usecases

import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.entities.Worktree
import app.tich.buildandrun.domain.failures.DomainFailure
import app.tich.buildandrun.domain.failures.DomainFailureMapper
import app.tich.buildandrun.domain.ports.GitClient
import app.tich.buildandrun.domain.ports.PreferencesStore

class AddRepositoryUseCase(
    private val gitClient: GitClient,
    private val preferencesStore: PreferencesStore,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val path = normalizePath(input.path)
        if (path.isBlank()) {
            return UseCaseResult.Failure(
                DomainFailure.Validation(
                    code = "app.validation.repository_path_blank",
                    reason = "repository_path_blank",
                    payload = mapOf("reason" to "repository_path_blank"),
                ),
            )
        }

        return try {
            val repositoryRoot = normalizePath(gitClient.getRepositoryRoot(atPath = path))
            val existingRepositories = preferencesStore.loadRepositories()
            if (existingRepositories.any { normalizePath(it.path) == repositoryRoot }) {
                return UseCaseResult.Failure(
                    DomainFailure.Conflict(
                        code = "app.repository_already_added",
                        payload = mapOf("path" to repositoryRoot),
                        isRetryable = false,
                    ),
                )
            }

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
        } catch (throwable: Throwable) {
            UseCaseResult.Failure(value = DomainFailureMapper.fromThrowable(throwable))
        }
    }

    data class Input(val path: String)

    data class Output(
        val repositories: List<Repository>,
        val addedRepository: Repository,
        val worktrees: List<Worktree>,
    )

    private fun normalizePath(path: String): String = path.trim().trimEnd('/')
}
