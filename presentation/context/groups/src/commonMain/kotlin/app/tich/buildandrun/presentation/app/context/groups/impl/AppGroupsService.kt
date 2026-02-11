package app.tich.buildandrun.presentation.app.context.groups.impl

import app.tich.buildandrun.application.context.repositories.usecase.*
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.presentation.app.AppGroupsFeature
import app.tich.buildandrun.presentation.app.context.state.MessagesContextState
import app.tich.buildandrun.presentation.app.context.state.RepositoriesContextState
import app.tich.buildandrun.presentation.app.core.AppErrorStateMapper
import app.tich.buildandrun.presentation.app.core.AppExecutionScope
import app.tich.buildandrun.presentation.app.core.AppStateRefresher
import kotlinx.coroutines.launch

class AppGroupsService(
    private val executionScope: AppExecutionScope,
    private val stateRefresher: AppStateRefresher,
    private val errorMapper: AppErrorStateMapper,
    private val repositoriesState: RepositoriesContextState,
    private val messagesState: MessagesContextState,
    private val reorderRepositoryGroupsUseCase: ReorderRepositoryGroupsUseCase,
    private val createRepositoryGroupUseCase: CreateRepositoryGroupUseCase,
    private val renameRepositoryGroupUseCase: RenameRepositoryGroupUseCase,
    private val deleteRepositoryGroupUseCase: DeleteRepositoryGroupUseCase,
    private val setRepositoryGroupUseCase: SetRepositoryGroupUseCase,
    private val createGroupAndAssignRepositoryUseCase: CreateGroupAndAssignRepositoryUseCase,
) : AppGroupsFeature {
    override fun onReorderRepositoryGroups(orderedGroupIds: List<String>) {
        executionScope.scope.launch {
            when (
                val result =
                    reorderRepositoryGroupsUseCase.execute(
                        input =
                            ReorderRepositoryGroupsUseCase.Input(
                                currentGroups = repositoriesState.repositoryGroups,
                                orderedGroupIds = orderedGroupIds,
                            ),
                    )
            ) {
                is UseCaseResult.Success -> {
                    repositoriesState.repositoryGroups = result.value.groups
                }

                is UseCaseResult.Failure -> {
                    messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                }
            }
            stateRefresher.publishAll()
        }
    }

    override fun onCreateRepositoryGroup(name: String) {
        executionScope.scope.launch {
            when (
                val result =
                    createRepositoryGroupUseCase.execute(
                        input =
                            CreateRepositoryGroupUseCase.Input(
                                name = name,
                                currentGroups = repositoriesState.repositoryGroups,
                            ),
                    )
            ) {
                is UseCaseResult.Success -> {
                    repositoriesState.repositoryGroups = result.value.groups
                }

                is UseCaseResult.Failure -> {
                    messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                }
            }
            stateRefresher.publishAll()
        }
    }

    override fun onRenameRepositoryGroup(
        groupId: String,
        newName: String,
    ) {
        executionScope.scope.launch {
            when (
                val result =
                    renameRepositoryGroupUseCase.execute(
                        input =
                            RenameRepositoryGroupUseCase.Input(
                                groupId = groupId,
                                newName = newName,
                                currentGroups = repositoriesState.repositoryGroups,
                            ),
                    )
            ) {
                is UseCaseResult.Success -> {
                    repositoriesState.repositoryGroups = result.value.groups
                }

                is UseCaseResult.Failure -> {
                    messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                }
            }
            stateRefresher.publishAll()
        }
    }

    override fun onDeleteRepositoryGroup(groupId: String) {
        executionScope.scope.launch {
            when (
                val result =
                    deleteRepositoryGroupUseCase.execute(
                        input =
                            DeleteRepositoryGroupUseCase.Input(
                                groupId = groupId,
                                currentGroups = repositoriesState.repositoryGroups,
                                currentRepositories = repositoriesState.repositories,
                            ),
                    )
            ) {
                is UseCaseResult.Success -> {
                    repositoriesState.repositoryGroups = result.value.groups
                    repositoriesState.repositories = result.value.repositories
                }

                is UseCaseResult.Failure -> {
                    messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                }
            }
            stateRefresher.publishAll()
        }
    }

    override fun onSetRepositoryGroup(
        repositoryId: String,
        groupId: String?,
    ) {
        executionScope.scope.launch {
            when (
                val result =
                    setRepositoryGroupUseCase.execute(
                        input =
                            SetRepositoryGroupUseCase.Input(
                                repositoryId = repositoryId,
                                groupId = groupId,
                            ),
                    )
            ) {
                is UseCaseResult.Success -> {
                    repositoriesState.repositories = result.value.repositories
                }

                is UseCaseResult.Failure -> {
                    messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                }
            }
            stateRefresher.publishAll()
        }
    }

    override fun onCreateGroupAndAssignRepository(
        name: String,
        repositoryId: String,
    ) {
        executionScope.scope.launch {
            when (
                val result =
                    createGroupAndAssignRepositoryUseCase.execute(
                        input =
                            CreateGroupAndAssignRepositoryUseCase.Input(
                                name = name,
                                repositoryId = repositoryId,
                                currentGroups = repositoriesState.repositoryGroups,
                                currentRepositories = repositoriesState.repositories,
                            ),
                    )
            ) {
                is UseCaseResult.Success -> {
                    repositoriesState.repositoryGroups = result.value.groups
                    repositoriesState.repositories = result.value.repositories
                    val assignmentFailure = result.value.assignmentFailure
                    if (assignmentFailure != null) {
                        messagesState.error = errorMapper.mapFailureToErrorState(assignmentFailure)
                    }
                }

                is UseCaseResult.Failure -> {
                    messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                }
            }
            stateRefresher.publishAll()
        }
    }
}
