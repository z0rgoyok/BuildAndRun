package app.tich.buildandrun.application.usecases

import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.entities.RepositoryGroupId
import app.tich.buildandrun.domain.failures.DomainFailureCode
import app.tich.buildandrun.testsupport.FakePreferencesStore
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SetRepositoryGroupUseCaseTest {
    @Test
    fun setsGroupIdWhenRepositoryExists() =
        runBlocking {
            val repository = Repository.create(path = "/tmp/repo-a")
            val preferencesStore = FakePreferencesStore(initialRepositories = listOf(repository))
            val useCase = SetRepositoryGroupUseCase(preferencesStore = preferencesStore)
            val groupId = RepositoryGroupId.generate()

            val result =
                useCase.execute(
                    input =
                        SetRepositoryGroupUseCase.Input(
                            repositoryId = repository.id.value,
                            groupId = groupId.value,
                        ),
                )

            assertTrue(result is UseCaseResult.Success<*>)
            val success = result as UseCaseResult.Success<SetRepositoryGroupUseCase.Output>
            assertEquals(groupId, success.value.repositories.single().groupId)
            assertEquals(groupId, preferencesStore.loadRepositories().single().groupId)
        }

    @Test
    fun clearsGroupIdWhenGroupIdIsNull() =
        runBlocking {
            val groupId = RepositoryGroupId.generate()
            val repository = Repository.create(path = "/tmp/repo-a", groupId = groupId)
            val preferencesStore = FakePreferencesStore(initialRepositories = listOf(repository))
            val useCase = SetRepositoryGroupUseCase(preferencesStore = preferencesStore)

            val result =
                useCase.execute(
                    input =
                        SetRepositoryGroupUseCase.Input(
                            repositoryId = repository.id.value,
                            groupId = null,
                        ),
                )

            assertTrue(result is UseCaseResult.Success<*>)
            val success = result as UseCaseResult.Success<SetRepositoryGroupUseCase.Output>
            assertNull(success.value.repositories.single().groupId)
        }

    @Test
    fun returnsValidationWhenRepositoryIdIsBlank() =
        runBlocking {
            val useCase = SetRepositoryGroupUseCase(preferencesStore = FakePreferencesStore())

            val result =
                useCase.execute(
                    input =
                        SetRepositoryGroupUseCase.Input(
                            repositoryId = " ",
                            groupId = "some-group",
                        ),
                )

            assertTrue(result is UseCaseResult.Failure)
            assertEquals(DomainFailureCode.APP_VALIDATION_REPOSITORY_ID_BLANK, result.value.code)
        }

    @Test
    fun returnsNotFoundWhenRepositoryDoesNotExist() =
        runBlocking {
            val useCase = SetRepositoryGroupUseCase(preferencesStore = FakePreferencesStore())

            val result =
                useCase.execute(
                    input =
                        SetRepositoryGroupUseCase.Input(
                            repositoryId = "missing",
                            groupId = "some-group",
                        ),
                )

            assertTrue(result is UseCaseResult.Failure)
            assertEquals(DomainFailureCode.APP_REPOSITORY_NOT_FOUND, result.value.code)
        }
}
