package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.context.repositories.model.RepositoryGroup
import app.tich.buildandrun.domain.shared.failure.DomainFailure

class CreateGroupAndAssignRepositoryUseCase(
    private val createRepositoryGroupUseCase: CreateRepositoryGroupUseCase,
    private val setRepositoryGroupUseCase: SetRepositoryGroupUseCase,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val createResult =
            when (
                val result =
                    createRepositoryGroupUseCase.execute(
                        input =
                            CreateRepositoryGroupUseCase.Input(
                                name = input.name,
                                currentGroups = input.currentGroups,
                            ),
                    )
            ) {
                is UseCaseResult.Success -> result.value
                is UseCaseResult.Failure -> return result
            }

        return when (
            val assignResult =
                setRepositoryGroupUseCase.execute(
                    input =
                        SetRepositoryGroupUseCase.Input(
                            repositoryId = input.repositoryId,
                            groupId = createResult.createdGroup.id.value,
                        ),
                )
        ) {
            is UseCaseResult.Success ->
                UseCaseResult.Success(
                    value =
                        Output(
                            groups = createResult.groups,
                            repositories = assignResult.value.repositories,
                            assignmentFailure = null,
                        ),
                )

            is UseCaseResult.Failure ->
                UseCaseResult.Success(
                    value =
                        Output(
                            groups = createResult.groups,
                            repositories = input.currentRepositories,
                            assignmentFailure = assignResult.value,
                        ),
                )
        }
    }

    data class Input(
        val name: String,
        val repositoryId: String,
        val currentGroups: List<RepositoryGroup>,
        val currentRepositories: List<Repository>,
    )

    data class Output(
        val groups: List<RepositoryGroup>,
        val repositories: List<Repository>,
        val assignmentFailure: DomainFailure?,
    )
}
