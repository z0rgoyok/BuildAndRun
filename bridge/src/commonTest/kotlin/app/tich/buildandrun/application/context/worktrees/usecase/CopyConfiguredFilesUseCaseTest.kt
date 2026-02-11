package app.tich.buildandrun.application.context.worktrees.usecase

import app.tich.buildandrun.application.context.shared.port.FileSystemHandling
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.copy.model.CopyPattern
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode
import app.tich.buildandrun.testsupport.FakePreferencesStore
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CopyConfiguredFilesUseCaseTest {
    @Test
    fun returnsValidationWhenRepositoryIdIsBlank() =
        runBlocking {
            val useCase =
                CopyConfiguredFilesUseCase(
                    preferencesStore = FakePreferencesStore(),
                    fileSystemHandling = FakeFileSystemHandling(),
                )

            val result =
                useCase.execute(
                    input =
                        CopyConfiguredFilesUseCase.Input(
                            repositoryPath = "/tmp/repo",
                            createdWorktreePath = "/tmp/repo-feature",
                            repositoryId = " ",
                        ),
                )

            assertTrue(result is UseCaseResult.Failure)
            assertEquals(DomainFailureCode.APP_VALIDATION_REPOSITORY_ID_BLANK, result.value.code)
        }

    @Test
    fun skipsMissingFilesAndReturnsSuccess() =
        runBlocking {
            val repositoryId = "repo-1"
            val preferencesStore = FakePreferencesStore()
            preferencesStore.setCopyPatterns(
                patterns = listOf(CopyPattern(pattern = ".env")),
                forRepositoryId = RepositoryId(repositoryId),
            )
            val useCase =
                CopyConfiguredFilesUseCase(
                    preferencesStore = preferencesStore,
                    fileSystemHandling = FakeFileSystemHandling(),
                )

            val result =
                useCase.execute(
                    input =
                        CopyConfiguredFilesUseCase.Input(
                            repositoryPath = "/tmp/repo",
                            createdWorktreePath = "/tmp/repo-feature",
                            repositoryId = repositoryId,
                        ),
                )

            assertTrue(result is UseCaseResult.Success<*>)
            val success = result as UseCaseResult.Success<CopyConfiguredFilesUseCase.Output>
            assertEquals(emptyList(), success.value.copiedPatterns)
        }

    @Test
    fun returnsFailureWhenCopyFails() =
        runBlocking {
            val repositoryId = "repo-1"
            val preferencesStore = FakePreferencesStore()
            preferencesStore.setCopyPatterns(
                patterns = listOf(CopyPattern(pattern = ".env")),
                forRepositoryId = RepositoryId(repositoryId),
            )
            val fileSystemHandling =
                FakeFileSystemHandling(
                    existingPaths = setOf("/tmp/repo/.env"),
                    failingCopyPaths = setOf("/tmp/repo/.env"),
                )
            val useCase =
                CopyConfiguredFilesUseCase(
                    preferencesStore = preferencesStore,
                    fileSystemHandling = fileSystemHandling,
                )

            val result =
                useCase.execute(
                    input =
                        CopyConfiguredFilesUseCase.Input(
                            repositoryPath = "/tmp/repo",
                            createdWorktreePath = "/tmp/repo-feature",
                            repositoryId = repositoryId,
                        ),
                )

            assertTrue(result is UseCaseResult.Failure)
            assertEquals(DomainFailureCode.APP_UNKNOWN, result.value.code)
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
