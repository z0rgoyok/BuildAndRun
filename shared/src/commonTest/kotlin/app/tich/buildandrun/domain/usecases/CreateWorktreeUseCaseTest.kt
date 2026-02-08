package app.tich.buildandrun.domain.usecases

import app.tich.buildandrun.testsupport.FakeGitClient
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateWorktreeUseCaseTest {
    @Test
    fun createsWorktreeWhenInputIsValid() =
        runBlocking {
            val gitClient = FakeGitClient().apply { registerRepository(path = "/tmp/repo") }
            val useCase = CreateWorktreeUseCase(gitClient = gitClient)

            val result =
                useCase.execute(
                    input =
                        CreateWorktreeUseCase.Input(
                            repositoryPath = "/tmp/repo",
                            branch = "feature/cleanup",
                            worktreePath = "/tmp/repo-feature-cleanup",
                            createBranch = true,
                            baseBranch = "main",
                        ),
                )

            assertTrue(result is UseCaseResult.Success<*>)
            val success = result as UseCaseResult.Success<CreateWorktreeUseCase.Output>
            assertEquals("/tmp/repo-feature-cleanup", success.value.createdWorktree.path)
            assertEquals("feature/cleanup", success.value.createdWorktree.branch)
            assertEquals(1, success.value.allWorktrees.size)
        }

    @Test
    fun returnsValidationWhenBranchIsBlank() =
        runBlocking {
            val useCase = CreateWorktreeUseCase(gitClient = FakeGitClient())

            val result =
                useCase.execute(
                    input =
                        CreateWorktreeUseCase.Input(
                            repositoryPath = "/tmp/repo",
                            branch = " ",
                            worktreePath = "/tmp/repo-feature-cleanup",
                            createBranch = true,
                            baseBranch = null,
                        ),
                )

            assertTrue(result is UseCaseResult.Failure)
            val failure = result
            assertEquals("app.validation.branch_blank", failure.value.code)
        }
}
