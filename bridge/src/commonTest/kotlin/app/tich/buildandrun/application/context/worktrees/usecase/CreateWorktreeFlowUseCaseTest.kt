package app.tich.buildandrun.application.context.worktrees.usecase

import app.tich.buildandrun.application.context.shared.port.FileSystemHandling
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.copy.model.CopyPattern
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode
import app.tich.buildandrun.testsupport.FakeGitClient
import app.tich.buildandrun.testsupport.FakePreferencesStore
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateWorktreeFlowUseCaseTest {
    @Test
    fun returnsValidationWhenRepositoryIdIsBlank() =
        runBlocking {
            val useCase =
                CreateWorktreeFlowUseCase(
                    createWorktreeUseCase = CreateWorktreeUseCase(gitClient = FakeGitClient()),
                    copyConfiguredFilesUseCase =
                        CopyConfiguredFilesUseCase(
                            preferencesStore = FakePreferencesStore(),
                            fileSystemHandling = FakeFileSystemHandling(),
                        ),
                    loadRepositoryWorktreesUseCase =
                        LoadRepositoryWorktreesUseCase(
                            gitClient = FakeGitClient(),
                            preferencesStore = FakePreferencesStore(),
                        ),
                    loadBranchesUseCase = LoadBranchesUseCase(gitClient = FakeGitClient()),
                    preferencesStore = FakePreferencesStore(),
                )

            val result =
                useCase.execute(
                    input =
                        CreateWorktreeFlowUseCase.Input(
                            repositoryId = " ",
                            repositoryPath = "/tmp/repo",
                            branch = "feature/cleanup",
                            worktreePath = "/tmp/repo-feature",
                            createBranch = true,
                            baseBranch = "main",
                        ),
                )

            assertTrue(result is UseCaseResult.Failure)
            assertEquals(DomainFailureCode.APP_VALIDATION_REPOSITORY_ID_BLANK, result.value.code)
        }

    @Test
    fun keepsSuccessWhenCopyConfiguredFilesFails() =
        runBlocking {
            val repositoryId = "repo-1"
            val gitClient = FakeGitClient().apply { registerRepository(path = "/tmp/repo") }
            val preferencesStore = FakePreferencesStore()
            preferencesStore.setCopyPatterns(
                patterns = listOf(CopyPattern(pattern = ".env")),
                forRepositoryId = RepositoryId(repositoryId),
            )
            val useCase =
                CreateWorktreeFlowUseCase(
                    createWorktreeUseCase = CreateWorktreeUseCase(gitClient = gitClient),
                    copyConfiguredFilesUseCase =
                        CopyConfiguredFilesUseCase(
                            preferencesStore = preferencesStore,
                            fileSystemHandling =
                                FakeFileSystemHandling(
                                    existingPaths = setOf("/tmp/repo/.env"),
                                    failingCopyPaths = setOf("/tmp/repo/.env"),
                                ),
                        ),
                    loadRepositoryWorktreesUseCase =
                        LoadRepositoryWorktreesUseCase(
                            gitClient = gitClient,
                            preferencesStore = preferencesStore,
                        ),
                    loadBranchesUseCase = LoadBranchesUseCase(gitClient = gitClient),
                    preferencesStore = preferencesStore,
                )

            val result =
                useCase.execute(
                    input =
                        CreateWorktreeFlowUseCase.Input(
                            repositoryId = repositoryId,
                            repositoryPath = "/tmp/repo",
                            branch = "feature/cleanup",
                            worktreePath = "/tmp/repo-feature",
                            createBranch = true,
                            baseBranch = "main",
                        ),
                )

            assertTrue(result is UseCaseResult.Success<*>)
            val success = result as UseCaseResult.Success<CreateWorktreeFlowUseCase.Output>
            assertEquals("/tmp/repo-feature", success.value.createdWorktree.path)
            assertTrue(success.value.worktrees.any { it.path == "/tmp/repo-feature" })
        }

    @Test
    fun keepsSuccessWhenLoadingWorktreesFailsAfterCreate() =
        runBlocking {
            val repositoryId = "repo-1"
            val createGitClient = FakeGitClient().apply { registerRepository(path = "/tmp/repo") }
            val failingSnapshotGitClient =
                FakeGitClient().apply {
                    registerRepository(path = "/tmp/repo")
                    failListWorktreesFor(repositoryPath = "/tmp/repo")
                }
            val preferencesStore = FakePreferencesStore()
            val useCase =
                CreateWorktreeFlowUseCase(
                    createWorktreeUseCase = CreateWorktreeUseCase(gitClient = createGitClient),
                    copyConfiguredFilesUseCase =
                        CopyConfiguredFilesUseCase(
                            preferencesStore = preferencesStore,
                            fileSystemHandling = FakeFileSystemHandling(),
                        ),
                    loadRepositoryWorktreesUseCase =
                        LoadRepositoryWorktreesUseCase(
                            gitClient = failingSnapshotGitClient,
                            preferencesStore = preferencesStore,
                        ),
                    loadBranchesUseCase = LoadBranchesUseCase(gitClient = createGitClient),
                    preferencesStore = preferencesStore,
                )

            val result =
                useCase.execute(
                    input =
                        CreateWorktreeFlowUseCase.Input(
                            repositoryId = repositoryId,
                            repositoryPath = "/tmp/repo",
                            branch = "feature/cleanup",
                            worktreePath = "/tmp/repo-feature",
                            createBranch = true,
                            baseBranch = "main",
                        ),
                )

            assertTrue(result is UseCaseResult.Success<*>)
            val success = result as UseCaseResult.Success<CreateWorktreeFlowUseCase.Output>
            assertTrue(success.value.worktrees.any { it.path == "/tmp/repo-feature" })
        }

    @Test
    fun keepsSuccessWhenLoadingBranchesFailsAfterCreate() =
        runBlocking {
            val repositoryId = "repo-1"
            val createGitClient = FakeGitClient().apply { registerRepository(path = "/tmp/repo") }
            val failingBranchesGitClient =
                FakeGitClient().apply {
                    registerRepository(path = "/tmp/repo")
                    failListBranchesFor(repositoryPath = "/tmp/repo")
                }
            val preferencesStore = FakePreferencesStore()
            val useCase =
                CreateWorktreeFlowUseCase(
                    createWorktreeUseCase = CreateWorktreeUseCase(gitClient = createGitClient),
                    copyConfiguredFilesUseCase =
                        CopyConfiguredFilesUseCase(
                            preferencesStore = preferencesStore,
                            fileSystemHandling = FakeFileSystemHandling(),
                        ),
                    loadRepositoryWorktreesUseCase =
                        LoadRepositoryWorktreesUseCase(
                            gitClient = createGitClient,
                            preferencesStore = preferencesStore,
                        ),
                    loadBranchesUseCase = LoadBranchesUseCase(gitClient = failingBranchesGitClient),
                    preferencesStore = preferencesStore,
                )

            val result =
                useCase.execute(
                    input =
                        CreateWorktreeFlowUseCase.Input(
                            repositoryId = repositoryId,
                            repositoryPath = "/tmp/repo",
                            branch = "feature/cleanup",
                            worktreePath = "/tmp/repo-feature",
                            createBranch = true,
                            baseBranch = "main",
                        ),
                )

            assertTrue(result is UseCaseResult.Success<*>)
            val success = result as UseCaseResult.Success<CreateWorktreeFlowUseCase.Output>
            assertTrue(success.value.branches.isEmpty())
            assertTrue(success.value.worktrees.any { it.path == "/tmp/repo-feature" })
        }

    private class FakeFileSystemHandling(
        private val existingPaths: Set<String> = emptySet(),
        private val failingCopyPaths: Set<String> = emptySet(),
    ) : FileSystemHandling {
        override suspend fun fileExists(atPath: String): Boolean = existingPaths.contains(atPath)

        override suspend fun isDirectory(atPath: String): Boolean = false

        override suspend fun createDirectory(
            atPath: String,
            withIntermediateDirectories: Boolean,
        ) = Unit

        override suspend fun copyItem(
            atPath: String,
            toPath: String,
        ) {
            if (failingCopyPaths.contains(atPath)) {
                throw IllegalStateException("copy failed")
            }
        }

        override suspend fun fileSize(atPath: String): Long? = null

        override suspend fun directorySize(atPath: String): Long? = null

        override suspend fun delete(
            atPath: String,
            recursive: Boolean,
        ) = Unit

        override suspend fun listDirectory(atPath: String): List<String> = emptyList()

        override fun homeDirectory(): String = "/tmp"
    }
}
