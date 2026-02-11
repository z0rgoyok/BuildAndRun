package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode
import app.tich.buildandrun.testsupport.FakePreferencesStore
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CreateGroupAndAssignRepositoryUseCaseTest {
    @Test
    fun assignsRepositoryWhenBothStepsSucceed() =
        runBlocking {
            val repository = Repository.create(path = "/tmp/repo-a")
            val preferencesStore = FakePreferencesStore(initialRepositories = listOf(repository))
            val useCase =
                CreateGroupAndAssignRepositoryUseCase(
                    createRepositoryGroupUseCase = CreateRepositoryGroupUseCase(preferencesStore = preferencesStore),
                    setRepositoryGroupUseCase = SetRepositoryGroupUseCase(preferencesStore = preferencesStore),
                )

            val result =
                useCase.execute(
                    input =
                        CreateGroupAndAssignRepositoryUseCase.Input(
                            name = "Backend",
                            repositoryId = repository.id.value,
                            currentGroups = emptyList(),
                            currentRepositories = listOf(repository),
                        ),
                )

            assertTrue(result is UseCaseResult.Success<*>)
            val success = result as UseCaseResult.Success<CreateGroupAndAssignRepositoryUseCase.Output>
            assertNull(success.value.assignmentFailure)
            assertEquals(1, success.value.groups.size)
            assertEquals(success.value.groups.single().id, success.value.repositories.single().groupId)
        }

    @Test
    fun returnsPartialSuccessWhenAssignmentFails() =
        runBlocking {
            val preferencesStore = FakePreferencesStore()
            val useCase =
                CreateGroupAndAssignRepositoryUseCase(
                    createRepositoryGroupUseCase = CreateRepositoryGroupUseCase(preferencesStore = preferencesStore),
                    setRepositoryGroupUseCase = SetRepositoryGroupUseCase(preferencesStore = preferencesStore),
                )

            val result =
                useCase.execute(
                    input =
                        CreateGroupAndAssignRepositoryUseCase.Input(
                            name = "Backend",
                            repositoryId = "missing",
                            currentGroups = emptyList(),
                            currentRepositories = emptyList(),
                        ),
                )

            assertTrue(result is UseCaseResult.Success<*>)
            val success = result as UseCaseResult.Success<CreateGroupAndAssignRepositoryUseCase.Output>
            assertEquals(1, success.value.groups.size)
            assertEquals(emptyList(), success.value.repositories)
            assertEquals(DomainFailureCode.APP_REPOSITORY_NOT_FOUND, success.value.assignmentFailure?.code)
        }
}
