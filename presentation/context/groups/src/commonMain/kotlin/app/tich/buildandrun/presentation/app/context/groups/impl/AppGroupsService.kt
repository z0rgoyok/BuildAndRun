package app.tich.buildandrun.presentation.app.context.groups.impl

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.repositories.usecase.SetRepositoryGroupUseCase
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.repositories.model.RepositoryGroup
import app.tich.buildandrun.domain.context.repositories.model.RepositoryGroupId
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode
import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper
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
    private val preferencesStore: PreferencesStore,
    private val setRepositoryGroupUseCase: SetRepositoryGroupUseCase,
) : AppGroupsFeature {
    override fun onReorderRepositoryGroups(orderedGroupIds: List<String>) {
        val groupById = repositoriesState.repositoryGroups.associateBy { it.id.value }
        val reordered =
            orderedGroupIds.mapIndexedNotNull { index, id ->
                groupById[id]?.copy(sortOrder = index)
            }
        val missingGroups = repositoriesState.repositoryGroups.filter { it.id.value !in orderedGroupIds }
        repositoriesState.repositoryGroups = reordered + missingGroups
        stateRefresher.publishAll()
        executionScope.scope.launch {
            runCatching {
                preferencesStore.saveRepositoryGroups(repositoriesState.repositoryGroups)
            }.onFailure { throwable ->
                messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
                stateRefresher.publishAll()
            }
        }
    }

    override fun onCreateRepositoryGroup(name: String) {
        val trimmedName = validateNewRepositoryGroupName(name) ?: return
        val nextSortOrder = (repositoriesState.repositoryGroups.maxOfOrNull { it.sortOrder } ?: -1) + 1
        val group = RepositoryGroup.create(name = trimmedName, sortOrder = nextSortOrder)
        repositoriesState.repositoryGroups += group
        executionScope.scope.launch {
            runCatching {
                preferencesStore.saveRepositoryGroups(repositoriesState.repositoryGroups)
            }.onFailure { throwable ->
                messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            }
            stateRefresher.publishAll()
        }
    }

    override fun onRenameRepositoryGroup(
        groupId: String,
        newName: String,
    ) {
        val trimmedName = newName.trim()
        if (trimmedName.isBlank()) {
            messagesState.error =
                errorMapper.mapFailureToErrorState(
                    DomainFailure.Validation(
                        code = DomainFailureCode.APP_GROUP_NAME_BLANK,
                        args = emptyList(),
                    ),
                )
            stateRefresher.publishAll()
            return
        }
        val groupIndex = repositoriesState.repositoryGroups.indexOfFirst { it.id.value == groupId }
        if (groupIndex == -1) {
            messagesState.error =
                errorMapper.mapFailureToErrorState(
                    DomainFailure.NotFound(
                        code = DomainFailureCode.APP_GROUP_NOT_FOUND,
                        args = emptyList(),
                        isRetryable = false,
                    ),
                )
            stateRefresher.publishAll()
            return
        }
        if (repositoriesState.repositoryGroups.any { it.id.value != groupId && it.name.equals(trimmedName, ignoreCase = true) }) {
            messagesState.error =
                errorMapper.mapFailureToErrorState(
                    DomainFailure.Conflict(
                        code = DomainFailureCode.APP_GROUP_NAME_DUPLICATE,
                        args = listOf(trimmedName),
                        isRetryable = false,
                    ),
                )
            stateRefresher.publishAll()
            return
        }
        repositoriesState.repositoryGroups =
            repositoriesState.repositoryGroups.toMutableList().apply {
                this[groupIndex] = this[groupIndex].copy(name = trimmedName)
            }
        executionScope.scope.launch {
            runCatching {
                preferencesStore.saveRepositoryGroups(repositoriesState.repositoryGroups)
            }.onFailure { throwable ->
                messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            }
            stateRefresher.publishAll()
        }
    }

    override fun onDeleteRepositoryGroup(groupId: String) {
        val removedGroupId = RepositoryGroupId(groupId)
        repositoriesState.repositoryGroups = repositoriesState.repositoryGroups.filter { it.id.value != groupId }
        repositoriesState.repositories =
            repositoriesState.repositories.map { repo ->
                if (repo.groupId == removedGroupId) repo.copy(groupId = null) else repo
            }
        executionScope.scope.launch {
            runCatching {
                preferencesStore.saveRepositoryGroups(repositoriesState.repositoryGroups)
                preferencesStore.saveRepositories(repositoriesState.repositories)
            }.onFailure { throwable ->
                messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
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
        val trimmedName = validateNewRepositoryGroupName(name) ?: return
        val nextSortOrder = (repositoriesState.repositoryGroups.maxOfOrNull { it.sortOrder } ?: -1) + 1
        val group = RepositoryGroup.create(name = trimmedName, sortOrder = nextSortOrder)
        repositoriesState.repositoryGroups += group
        executionScope.scope.launch {
            runCatching {
                preferencesStore.saveRepositoryGroups(repositoriesState.repositoryGroups)
                when (
                    val result =
                        setRepositoryGroupUseCase.execute(
                            input =
                                SetRepositoryGroupUseCase.Input(
                                    repositoryId = repositoryId,
                                    groupId = group.id.value,
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
            }.onFailure { throwable ->
                messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            }
            stateRefresher.publishAll()
        }
    }

    private fun validateNewRepositoryGroupName(name: String): String? {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            messagesState.error =
                errorMapper.mapFailureToErrorState(
                    DomainFailure.Validation(
                        code = DomainFailureCode.APP_GROUP_NAME_BLANK,
                        args = emptyList(),
                    ),
                )
            stateRefresher.publishAll()
            return null
        }
        if (repositoriesState.repositoryGroups.any { it.name.equals(trimmedName, ignoreCase = true) }) {
            messagesState.error =
                errorMapper.mapFailureToErrorState(
                    DomainFailure.Conflict(
                        code = DomainFailureCode.APP_GROUP_NAME_DUPLICATE,
                        args = listOf(trimmedName),
                        isRetryable = false,
                    ),
                )
            stateRefresher.publishAll()
            return null
        }
        return trimmedName
    }
}
