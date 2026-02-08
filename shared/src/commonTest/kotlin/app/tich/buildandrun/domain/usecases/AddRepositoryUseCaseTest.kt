package app.tich.buildandrun.domain.usecases

import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.entities.Worktree
import app.tich.buildandrun.testsupport.FakeGitClient
import app.tich.buildandrun.testsupport.FakePreferencesStore
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddRepositoryUseCaseTest {
    @Test
    fun addsRepositoryWhenItIsValid() =
        runBlocking {
            val gitClient = FakeGitClient()
            gitClient.registerRepository(path = "/tmp/repo")
            gitClient.setWorktrees(
                repositoryPath = "/tmp/repo",
                worktrees =
                    listOf(
                        Worktree(
                            path = "/tmp/repo",
                            branch = "main",
                            isMain = true,
                            commitHash = null,
                            isLocked = false,
                            isPrunable = false,
                            baseBranch = null,
                        ),
                    ),
            )
            val preferencesStore = FakePreferencesStore()
            val useCase = AddRepositoryUseCase(gitClient = gitClient, preferencesStore = preferencesStore)

            val result = useCase.execute(input = AddRepositoryUseCase.Input(path = " /tmp/repo/ "))

            assertTrue(result is UseCaseResult.Success<*>)
            val success = result as UseCaseResult.Success<AddRepositoryUseCase.Output>
            assertEquals(1, success.value.repositories.size)
            assertEquals("/tmp/repo", success.value.addedRepository.path)
            assertEquals(1, success.value.worktrees.size)
            assertEquals(1, preferencesStore.loadRepositories().size)
        }

    @Test
    fun returnsConflictWhenRepositoryAlreadyExists() =
        runBlocking {
            val existingRepository = Repository.create(path = "/tmp/repo")
            val gitClient = FakeGitClient().apply { registerRepository(path = "/tmp/repo") }
            val preferencesStore = FakePreferencesStore(initialRepositories = listOf(existingRepository))
            val useCase = AddRepositoryUseCase(gitClient = gitClient, preferencesStore = preferencesStore)

            val result = useCase.execute(input = AddRepositoryUseCase.Input(path = "/tmp/repo"))

            assertTrue(result is UseCaseResult.Failure)
            val failure = result
            assertEquals("app.repository_already_added", failure.value.code)
        }

    @Test
    fun returnsValidationWhenPathIsBlank() =
        runBlocking {
            val useCase =
                AddRepositoryUseCase(
                    gitClient = FakeGitClient(),
                    preferencesStore = FakePreferencesStore(),
                )

            val result = useCase.execute(input = AddRepositoryUseCase.Input(path = " "))

            assertTrue(result is UseCaseResult.Failure)
            val failure = result
            assertEquals("app.validation.repository_path_blank", failure.value.code)
        }
}
