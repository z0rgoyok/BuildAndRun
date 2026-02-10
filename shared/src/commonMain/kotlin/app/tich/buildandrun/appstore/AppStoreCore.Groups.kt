package app.tich.buildandrun.appstore

import app.tich.buildandrun.application.usecases.SetRepositoryGroupUseCase
import app.tich.buildandrun.application.usecases.UseCaseResult
import app.tich.buildandrun.domain.entities.RepositoryGroup
import app.tich.buildandrun.domain.entities.RepositoryGroupId
import app.tich.buildandrun.domain.failures.DomainFailure
import app.tich.buildandrun.domain.failures.DomainFailureCode
import app.tich.buildandrun.domain.failures.DomainFailureMapper
import kotlinx.coroutines.launch

internal fun AppStoreCore.onCreateRepositoryGroup(name: String) {
    val trimmedName = name.trim()
    if (trimmedName.isBlank()) {
        error = mapFailureToErrorState(
            DomainFailure.Validation(
                code = DomainFailureCode.APP_GROUP_NAME_BLANK,
                args = emptyList(),
            ),
        )
        publishState()
        return
    }
    if (repositoryGroups.any { it.name.equals(trimmedName, ignoreCase = true) }) {
        error = mapFailureToErrorState(
            DomainFailure.Conflict(
                code = DomainFailureCode.APP_GROUP_NAME_DUPLICATE,
                args = listOf(trimmedName),
                isRetryable = false,
            ),
        )
        publishState()
        return
    }
    val nextSortOrder = (repositoryGroups.maxOfOrNull { it.sortOrder } ?: -1) + 1
    val group = RepositoryGroup.create(name = trimmedName, sortOrder = nextSortOrder)
    repositoryGroups = repositoryGroups + group
    scope.launch {
        runCatching {
            graph.preferencesStore.saveRepositoryGroups(repositoryGroups)
        }.onFailure { throwable ->
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        publishState()
    }
}

internal fun AppStoreCore.onRenameRepositoryGroup(
    groupId: String,
    newName: String,
) {
    val trimmedName = newName.trim()
    if (trimmedName.isBlank()) {
        error = mapFailureToErrorState(
            DomainFailure.Validation(
                code = DomainFailureCode.APP_GROUP_NAME_BLANK,
                args = emptyList(),
            ),
        )
        publishState()
        return
    }
    val groupIndex = repositoryGroups.indexOfFirst { it.id.value == groupId }
    if (groupIndex == -1) {
        error = mapFailureToErrorState(
            DomainFailure.NotFound(
                code = DomainFailureCode.APP_GROUP_NOT_FOUND,
                args = emptyList(),
                isRetryable = false,
            ),
        )
        publishState()
        return
    }
    if (repositoryGroups.any { it.id.value != groupId && it.name.equals(trimmedName, ignoreCase = true) }) {
        error = mapFailureToErrorState(
            DomainFailure.Conflict(
                code = DomainFailureCode.APP_GROUP_NAME_DUPLICATE,
                args = listOf(trimmedName),
                isRetryable = false,
            ),
        )
        publishState()
        return
    }
    repositoryGroups = repositoryGroups.toMutableList().apply {
        this[groupIndex] = this[groupIndex].copy(name = trimmedName)
    }
    scope.launch {
        runCatching {
            graph.preferencesStore.saveRepositoryGroups(repositoryGroups)
        }.onFailure { throwable ->
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        publishState()
    }
}

internal fun AppStoreCore.onDeleteRepositoryGroup(groupId: String) {
    val removedGroupId = RepositoryGroupId(groupId)
    repositoryGroups = repositoryGroups.filter { it.id.value != groupId }
    repositories = repositories.map { repo ->
        if (repo.groupId == removedGroupId) repo.copy(groupId = null) else repo
    }
    scope.launch {
        runCatching {
            graph.preferencesStore.saveRepositoryGroups(repositoryGroups)
            graph.preferencesStore.saveRepositories(repositories)
        }.onFailure { throwable ->
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        publishState()
    }
}

internal fun AppStoreCore.onSetRepositoryGroup(
    repositoryId: String,
    groupId: String?,
) {
    scope.launch {
        when (
            val result = graph.setRepositoryGroupUseCase.execute(
                input = SetRepositoryGroupUseCase.Input(
                    repositoryId = repositoryId,
                    groupId = groupId,
                ),
            )
        ) {
            is UseCaseResult.Success -> {
                repositories = result.value.repositories
            }
            is UseCaseResult.Failure -> {
                error = mapFailureToErrorState(result.value)
            }
        }
        publishState()
    }
}

internal fun AppStoreCore.onCreateGroupAndAssignRepository(
    name: String,
    repositoryId: String,
) {
    val trimmedName = name.trim()
    if (trimmedName.isBlank()) {
        error = mapFailureToErrorState(
            DomainFailure.Validation(
                code = DomainFailureCode.APP_GROUP_NAME_BLANK,
                args = emptyList(),
            ),
        )
        publishState()
        return
    }
    if (repositoryGroups.any { it.name.equals(trimmedName, ignoreCase = true) }) {
        error = mapFailureToErrorState(
            DomainFailure.Conflict(
                code = DomainFailureCode.APP_GROUP_NAME_DUPLICATE,
                args = listOf(trimmedName),
                isRetryable = false,
            ),
        )
        publishState()
        return
    }
    val nextSortOrder = (repositoryGroups.maxOfOrNull { it.sortOrder } ?: -1) + 1
    val group = RepositoryGroup.create(name = trimmedName, sortOrder = nextSortOrder)
    repositoryGroups = repositoryGroups + group
    scope.launch {
        runCatching {
            graph.preferencesStore.saveRepositoryGroups(repositoryGroups)
            when (
                val result = graph.setRepositoryGroupUseCase.execute(
                    input = SetRepositoryGroupUseCase.Input(
                        repositoryId = repositoryId,
                        groupId = group.id.value,
                    ),
                )
            ) {
                is UseCaseResult.Success -> {
                    repositories = result.value.repositories
                }
                is UseCaseResult.Failure -> {
                    error = mapFailureToErrorState(result.value)
                }
            }
        }.onFailure { throwable ->
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        publishState()
    }
}
