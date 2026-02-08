package app.tich.buildandrun.application.usecases

import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.failures.DomainFailureCode
import app.tich.buildandrun.testsupport.FakePreferencesStore
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SetRepositoryArchivedStateUseCaseTest {
    @Test
    fun archivesRepositoryWhenRepositoryExists() =
        runBlocking {
            val repository = Repository.create(path = "/tmp/repo-a")
            val preferencesStore = FakePreferencesStore(initialRepositories = listOf(repository))
            val useCase = SetRepositoryArchivedStateUseCase(preferencesStore = preferencesStore)

            val result =
                useCase.execute(
                    input =
                        SetRepositoryArchivedStateUseCase.Input(
                            repositoryId = repository.id.value,
                            isArchived = true,
                        ),
                )

            assertTrue(result is UseCaseResult.Success<*>)
            val success = result as UseCaseResult.Success<SetRepositoryArchivedStateUseCase.Output>
            assertTrue(success.value.updatedRepository.isArchived)
            assertTrue(preferencesStore.loadRepositories().single().isArchived)
        }

    @Test
    fun restoresRepositoryWhenRepositoryExists() =
        runBlocking {
            val archivedRepository = Repository.create(path = "/tmp/repo-a", isArchived = true)
            val preferencesStore = FakePreferencesStore(initialRepositories = listOf(archivedRepository))
            val useCase = SetRepositoryArchivedStateUseCase(preferencesStore = preferencesStore)

            val result =
                useCase.execute(
                    input =
                        SetRepositoryArchivedStateUseCase.Input(
                            repositoryId = archivedRepository.id.value,
                            isArchived = false,
                        ),
                )

            assertTrue(result is UseCaseResult.Success<*>)
            val success = result as UseCaseResult.Success<SetRepositoryArchivedStateUseCase.Output>
            assertFalse(success.value.updatedRepository.isArchived)
            assertFalse(preferencesStore.loadRepositories().single().isArchived)
        }

    @Test
    fun returnsValidationWhenRepositoryIdIsBlank() =
        runBlocking {
            val useCase = SetRepositoryArchivedStateUseCase(preferencesStore = FakePreferencesStore())

            val result =
                useCase.execute(
                    input =
                        SetRepositoryArchivedStateUseCase.Input(
                            repositoryId = " ",
                            isArchived = true,
                        ),
                )

            assertTrue(result is UseCaseResult.Failure)
            assertEquals(DomainFailureCode.APP_VALIDATION_REPOSITORY_ID_BLANK, result.value.code)
        }

    @Test
    fun returnsNotFoundWhenRepositoryDoesNotExist() =
        runBlocking {
            val useCase = SetRepositoryArchivedStateUseCase(preferencesStore = FakePreferencesStore())

            val result =
                useCase.execute(
                    input =
                        SetRepositoryArchivedStateUseCase.Input(
                            repositoryId = "missing",
                            isArchived = true,
                        ),
                )

            assertTrue(result is UseCaseResult.Failure)
            assertEquals(DomainFailureCode.APP_REPOSITORY_NOT_FOUND, result.value.code)
        }
}
