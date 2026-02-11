package app.tich.buildandrun.application.context.worktrees.usecase

import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.worktrees.model.CompleteWorktreeOptions
import app.tich.buildandrun.testsupport.FakeGitClient
import app.tich.buildandrun.testsupport.FakePreferencesStore
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompleteWorktreeUseCaseTest {
    @Test
    fun pullsTargetBeforeMergeWhenPullTargetFirstIsEnabled() =
        runBlocking {
            val gitClient = FakeGitClient().apply { registerRepository(path = "/tmp/repo") }
            val preferencesStore = FakePreferencesStore()
            gitClient.createWorktree(
                atRepoPath = "/tmp/repo",
                worktreePath = "/tmp/repo-main",
                branch = "main",
                createBranch = false,
                baseBranch = null,
            )
            gitClient.createWorktree(
                atRepoPath = "/tmp/repo",
                worktreePath = "/tmp/repo-feature",
                branch = "feature/cleanup",
                createBranch = true,
                baseBranch = "main",
            )
            val sourceWorktree = gitClient.listWorktrees(atRepoPath = "/tmp/repo").first { it.path == "/tmp/repo-feature" }
            val useCase =
                CompleteWorktreeUseCase(
                    gitClient = gitClient,
                    preferencesStore = preferencesStore,
                )

            val result =
                useCase.execute(
                    input =
                        CompleteWorktreeUseCase.Input(
                            repositoryPath = "/tmp/repo",
                            worktree = sourceWorktree,
                            options =
                                CompleteWorktreeOptions(
                                    targetBranch = "main",
                                    mergeIntoTarget = true,
                                    pullTargetFirst = true,
                                    deleteLocalBranch = false,
                                    deleteRemoteBranch = false,
                                    force = false,
                                ),
                        ),
                )

            assertTrue(result is UseCaseResult.Success<*>)
            assertEquals(
                listOf(
                    "pull:/tmp/repo-main",
                    "mergeBranch:feature/cleanup->main",
                    "removeWorktree:/tmp/repo-feature",
                ),
                gitClient.operations,
            )
        }

    @Test
    fun skipsPullWhenTargetWorktreeIsMissing() =
        runBlocking {
            val gitClient = FakeGitClient().apply { registerRepository(path = "/tmp/repo") }
            val preferencesStore = FakePreferencesStore()
            gitClient.createWorktree(
                atRepoPath = "/tmp/repo",
                worktreePath = "/tmp/repo-feature",
                branch = "feature/cleanup",
                createBranch = true,
                baseBranch = "main",
            )
            val sourceWorktree = gitClient.listWorktrees(atRepoPath = "/tmp/repo").first { it.path == "/tmp/repo-feature" }
            val useCase =
                CompleteWorktreeUseCase(
                    gitClient = gitClient,
                    preferencesStore = preferencesStore,
                )

            val result =
                useCase.execute(
                    input =
                        CompleteWorktreeUseCase.Input(
                            repositoryPath = "/tmp/repo",
                            worktree = sourceWorktree,
                            options =
                                CompleteWorktreeOptions(
                                    targetBranch = "main",
                                    mergeIntoTarget = true,
                                    pullTargetFirst = true,
                                    deleteLocalBranch = false,
                                    deleteRemoteBranch = false,
                                    force = false,
                                ),
                        ),
                )

            assertTrue(result is UseCaseResult.Success<*>)
            assertEquals(
                listOf(
                    "mergeBranch:feature/cleanup->main",
                    "removeWorktree:/tmp/repo-feature",
                ),
                gitClient.operations,
            )
        }
}
