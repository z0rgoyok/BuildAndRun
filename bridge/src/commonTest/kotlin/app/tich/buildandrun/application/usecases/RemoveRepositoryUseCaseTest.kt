package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode
import app.tich.buildandrun.testsupport.FakePreferencesStore
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RemoveRepositoryUseCaseTest {
    @Test
    fun removesRepositoryWhenRepositoryExists() =
        runBlocking {
            val repositoryA = Repository.create(path = "/tmp/repo-a")
            val repositoryB = Repository.create(path = "/tmp/repo-b")
            val preferencesStore = FakePreferencesStore(initialRepositories = listOf(repositoryA, repositoryB))
            val useCase = RemoveRepositoryUseCase(preferencesStore = preferencesStore)

            val result = useCase.execute(input = RemoveRepositoryUseCase.Input(repositoryId = repositoryA.id.value))

            assertTrue(result is UseCaseResult.Success<*>)
            val success = result as UseCaseResult.Success<RemoveRepositoryUseCase.Output>
            assertEquals(repositoryA.id.value, success.value.removedRepository.id.value)
            assertEquals(listOf(repositoryB.id.value), success.value.repositories.map { it.id.value })
            assertEquals(listOf(repositoryB.id.value), preferencesStore.loadRepositories().map { it.id.value })
        }

    @Test
    fun returnsValidationWhenRepositoryIdIsBlank() =
        runBlocking {
            val useCase = RemoveRepositoryUseCase(preferencesStore = FakePreferencesStore())

            val result = useCase.execute(input = RemoveRepositoryUseCase.Input(repositoryId = " "))

            assertTrue(result is UseCaseResult.Failure)
            assertEquals(DomainFailureCode.APP_VALIDATION_REPOSITORY_ID_BLANK, result.value.code)
        }

    @Test
    fun returnsNotFoundWhenRepositoryDoesNotExist() =
        runBlocking {
            val repository = Repository.create(path = "/tmp/repo-a")
            val preferencesStore = FakePreferencesStore(initialRepositories = listOf(repository))
            val useCase = RemoveRepositoryUseCase(preferencesStore = preferencesStore)

            val result = useCase.execute(input = RemoveRepositoryUseCase.Input(repositoryId = "missing"))

            assertTrue(result is UseCaseResult.Failure)
            assertEquals(DomainFailureCode.APP_REPOSITORY_NOT_FOUND, result.value.code)
        }
}
