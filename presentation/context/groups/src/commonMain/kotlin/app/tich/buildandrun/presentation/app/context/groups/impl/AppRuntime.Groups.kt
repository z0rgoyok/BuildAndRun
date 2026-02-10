package app.tich.buildandrun.presentation.app.context.groups.impl

import app.tich.buildandrun.application.context.repositories.usecase.SetRepositoryGroupUseCase
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.repositories.model.RepositoryGroup
import app.tich.buildandrun.domain.context.repositories.model.RepositoryGroupId
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode
import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper
import app.tich.buildandrun.presentation.app.core.AppWiring
import app.tich.buildandrun.presentation.app.core.mapFailureToErrorState
import app.tich.buildandrun.presentation.app.core.publishState
import kotlinx.coroutines.launch

internal fun AppWiring.onCreateRepositoryGroup(name: String) {
    val trimmedName = validateNewRepositoryGroupName(name) ?: return
    val nextSortOrder = (repositoriesState.repositoryGroups.maxOfOrNull { it.sortOrder } ?: -1) + 1
    val group = RepositoryGroup.create(name = trimmedName, sortOrder = nextSortOrder)
    repositoriesState.repositoryGroups = repositoriesState.repositoryGroups + group
    scope.launch {
        runCatching {
            graph.preferencesStore.saveRepositoryGroups(repositoriesState.repositoryGroups)
        }.onFailure { throwable ->
            messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        publishState()
    }
}

internal fun AppWiring.onRenameRepositoryGroup(
    groupId: String,
    newName: String,
) {
    val trimmedName = newName.trim()
    if (trimmedName.isBlank()) {
        messagesState.error =
            mapFailureToErrorState(
                DomainFailure.Validation(
                    code = DomainFailureCode.APP_GROUP_NAME_BLANK,
                    args = emptyList(),
                ),
            )
        publishState()
        return
    }
    val groupIndex = repositoriesState.repositoryGroups.indexOfFirst { it.id.value == groupId }
    if (groupIndex == -1) {
        messagesState.error =
            mapFailureToErrorState(
                DomainFailure.NotFound(
                    code = DomainFailureCode.APP_GROUP_NOT_FOUND,
                    args = emptyList(),
                    isRetryable = false,
                ),
            )
        publishState()
        return
    }
    if (repositoriesState.repositoryGroups.any { it.id.value != groupId && it.name.equals(trimmedName, ignoreCase = true) }) {
        messagesState.error =
            mapFailureToErrorState(
                DomainFailure.Conflict(
                    code = DomainFailureCode.APP_GROUP_NAME_DUPLICATE,
                    args = listOf(trimmedName),
                    isRetryable = false,
                ),
            )
        publishState()
        return
    }
    repositoriesState.repositoryGroups =
        repositoriesState.repositoryGroups.toMutableList().apply {
            this[groupIndex] = this[groupIndex].copy(name = trimmedName)
        }
    scope.launch {
        runCatching {
            graph.preferencesStore.saveRepositoryGroups(repositoriesState.repositoryGroups)
        }.onFailure { throwable ->
            messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        publishState()
    }
}

internal fun AppWiring.onDeleteRepositoryGroup(groupId: String) {
    val removedGroupId = RepositoryGroupId(groupId)
    repositoriesState.repositoryGroups = repositoriesState.repositoryGroups.filter { it.id.value != groupId }
    repositoriesState.repositories =
        repositoriesState.repositories.map { repo ->
            if (repo.groupId == removedGroupId) repo.copy(groupId = null) else repo
        }
    scope.launch {
        runCatching {
            graph.preferencesStore.saveRepositoryGroups(repositoriesState.repositoryGroups)
            graph.preferencesStore.saveRepositories(repositoriesState.repositories)
        }.onFailure { throwable ->
            messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        publishState()
    }
}

internal fun AppWiring.onSetRepositoryGroup(
    repositoryId: String,
    groupId: String?,
) {
    scope.launch {
        when (
            val result =
                graph.setRepositoryGroupUseCase.execute(
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
                messagesState.error = mapFailureToErrorState(result.value)
            }
        }
        publishState()
    }
}

internal fun AppWiring.onReorderRepositoryGroups(orderedGroupIds: List<String>) {
    val groupById = repositoriesState.repositoryGroups.associateBy { it.id.value }
    val reordered =
        orderedGroupIds.mapIndexedNotNull { index, id ->
            groupById[id]?.copy(sortOrder = index)
        }
    val missingGroups = repositoriesState.repositoryGroups.filter { it.id.value !in orderedGroupIds }
    repositoriesState.repositoryGroups = reordered + missingGroups
    publishState()
    scope.launch {
        runCatching {
            graph.preferencesStore.saveRepositoryGroups(repositoriesState.repositoryGroups)
        }.onFailure { throwable ->
            messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            publishState()
        }
    }
}

internal fun AppWiring.onCreateGroupAndAssignRepository(
    name: String,
    repositoryId: String,
) {
    val trimmedName = validateNewRepositoryGroupName(name) ?: return
    val nextSortOrder = (repositoriesState.repositoryGroups.maxOfOrNull { it.sortOrder } ?: -1) + 1
    val group = RepositoryGroup.create(name = trimmedName, sortOrder = nextSortOrder)
    repositoriesState.repositoryGroups = repositoriesState.repositoryGroups + group
    scope.launch {
        runCatching {
            graph.preferencesStore.saveRepositoryGroups(repositoriesState.repositoryGroups)
            when (
                val result =
                    graph.setRepositoryGroupUseCase.execute(
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
                    messagesState.error = mapFailureToErrorState(result.value)
                }
            }
        }.onFailure { throwable ->
            messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        publishState()
    }
}

private fun AppWiring.validateNewRepositoryGroupName(name: String): String? {
    val trimmedName = name.trim()
    if (trimmedName.isBlank()) {
        messagesState.error =
            mapFailureToErrorState(
                DomainFailure.Validation(
                    code = DomainFailureCode.APP_GROUP_NAME_BLANK,
                    args = emptyList(),
                ),
            )
        publishState()
        return null
    }
    if (repositoriesState.repositoryGroups.any { it.name.equals(trimmedName, ignoreCase = true) }) {
        messagesState.error =
            mapFailureToErrorState(
                DomainFailure.Conflict(
                    code = DomainFailureCode.APP_GROUP_NAME_DUPLICATE,
                    args = listOf(trimmedName),
                    isRetryable = false,
                ),
            )
        publishState()
        return null
    }
    return trimmedName
}
