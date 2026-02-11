package app.tich.buildandrun.presentation.app.context.sidebar.impl

import app.tich.buildandrun.application.context.repositories.usecase.SetSidebarMembershipStateUseCase
import app.tich.buildandrun.application.context.repositories.usecase.SyncSidebarSelectionExpansionUseCase
import app.tich.buildandrun.application.context.repositories.usecase.ToggleSidebarRepositoriesExpansionUseCase
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.presentation.app.AppSidebarFeature
import app.tich.buildandrun.presentation.app.context.state.MessagesContextState
import app.tich.buildandrun.presentation.app.context.state.RepositoriesContextState
import app.tich.buildandrun.presentation.app.core.AppErrorStateMapper
import app.tich.buildandrun.presentation.app.core.AppStateRefresher

class AppSidebarService(
    private val stateRefresher: AppStateRefresher,
    private val errorMapper: AppErrorStateMapper,
    private val repositoriesState: RepositoriesContextState,
    private val messagesState: MessagesContextState,
    private val setSidebarMembershipStateUseCase: SetSidebarMembershipStateUseCase,
    private val toggleSidebarRepositoriesExpansionUseCase: ToggleSidebarRepositoriesExpansionUseCase,
    private val syncSidebarSelectionExpansionUseCase: SyncSidebarSelectionExpansionUseCase,
) : AppSidebarFeature {
    override fun onSetSidebarRepositoryExpanded(
        repositoryId: String,
        expanded: Boolean,
    ) {
        when (
            val result =
                setSidebarMembershipStateUseCase.execute(
                    input =
                        SetSidebarMembershipStateUseCase.Input(
                            target = SetSidebarMembershipStateUseCase.Target.EXPANDED_REPOSITORIES,
                            id = repositoryId,
                            enabled = expanded,
                            currentIds = repositoriesState.expandedRepositoryIds,
                        ),
                )
        ) {
            is UseCaseResult.Success -> {
                if (repositoriesState.expandedRepositoryIds != result.value.ids) {
                    repositoriesState.expandedRepositoryIds = result.value.ids
                    stateRefresher.publishAll()
                }
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                stateRefresher.publishAll()
            }
        }
    }

    override fun onSetSidebarGroupCollapsed(
        groupId: String,
        collapsed: Boolean,
    ) {
        when (
            val result =
                setSidebarMembershipStateUseCase.execute(
                    input =
                        SetSidebarMembershipStateUseCase.Input(
                            target = SetSidebarMembershipStateUseCase.Target.COLLAPSED_GROUPS,
                            id = groupId,
                            enabled = collapsed,
                            currentIds = repositoriesState.collapsedGroupIds,
                        ),
                )
        ) {
            is UseCaseResult.Success -> {
                if (repositoriesState.collapsedGroupIds != result.value.ids) {
                    repositoriesState.collapsedGroupIds = result.value.ids
                    stateRefresher.publishAll()
                }
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                stateRefresher.publishAll()
            }
        }
    }

    override fun onToggleVisibleSidebarRepositoriesExpansion(
        includeArchivedRepositories: Boolean,
        preferredRepositoryId: String?,
    ) {
        onToggleSidebarRepositoriesExpansion(
            repositoryIds = visibleSidebarRepositoryIds(includeArchivedRepositories = includeArchivedRepositories),
            preferredRepositoryId = preferredRepositoryId,
        )
    }

    override fun onSyncSidebarSelectionExpansion(repositoryId: String?) {
        when (
            val result =
                syncSidebarSelectionExpansionUseCase.execute(
                    input =
                        SyncSidebarSelectionExpansionUseCase.Input(
                            repositoryId = repositoryId,
                            currentExpandedRepositoryIds = repositoriesState.expandedRepositoryIds,
                        ),
                )
        ) {
            is UseCaseResult.Success -> {
                if (result.value.changed) {
                    repositoriesState.expandedRepositoryIds = result.value.expandedRepositoryIds
                    stateRefresher.publishAll()
                }
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                stateRefresher.publishAll()
            }
        }
    }

    override fun areVisibleSidebarRepositoriesExpanded(includeArchivedRepositories: Boolean): Boolean =
        areSidebarRepositoriesExpanded(
            repositoryIds = visibleSidebarRepositoryIds(includeArchivedRepositories = includeArchivedRepositories),
        )

    override fun hasVisibleSidebarRepositories(includeArchivedRepositories: Boolean): Boolean =
        visibleSidebarRepositoryIds(includeArchivedRepositories = includeArchivedRepositories).isNotEmpty()

    private fun onToggleSidebarRepositoriesExpansion(
        repositoryIds: Set<String>,
        preferredRepositoryId: String?,
    ) {
        when (
            val result =
                toggleSidebarRepositoriesExpansionUseCase.execute(
                    input =
                        ToggleSidebarRepositoriesExpansionUseCase.Input(
                            repositoryIds = repositoryIds,
                            preferredRepositoryId = preferredRepositoryId?.takeIf { it.isNotBlank() },
                            currentExpandedRepositoryIds = repositoriesState.expandedRepositoryIds,
                        ),
                )
        ) {
            is UseCaseResult.Success -> {
                if (repositoriesState.expandedRepositoryIds != result.value.expandedRepositoryIds) {
                    repositoriesState.expandedRepositoryIds = result.value.expandedRepositoryIds
                    stateRefresher.publishAll()
                }
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                stateRefresher.publishAll()
            }
        }
    }

    private fun areSidebarRepositoriesExpanded(repositoryIds: Set<String>): Boolean =
        repositoryIds.isNotEmpty() && repositoryIds.all(repositoriesState.expandedRepositoryIds::contains)

    private fun visibleSidebarRepositoryIds(includeArchivedRepositories: Boolean): Set<String> =
        repositoriesState.repositories
            .asSequence()
            .filter { repository ->
                if (repository.isArchived) {
                    includeArchivedRepositories
                } else {
                    val groupId = repository.groupId?.value
                    groupId == null || !repositoriesState.collapsedGroupIds.contains(groupId)
                }
            }.map { repository -> repository.id.value }
            .toSet()
}
