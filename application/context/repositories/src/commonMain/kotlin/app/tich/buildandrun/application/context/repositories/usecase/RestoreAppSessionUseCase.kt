package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.domain.context.copy.model.CopyPattern
import app.tich.buildandrun.domain.context.kanban.model.KanbanTask
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.context.repositories.model.RepositoryGroup

class RestoreAppSessionUseCase(
    private val preferencesStore: PreferencesStore,
    private val loadRepositoriesUseCase: LoadRepositoriesUseCase,
) {
    suspend fun execute(): UseCaseResult<Output> {
        return when (val repositoriesResult = loadRepositoriesUseCase.execute()) {
            is UseCaseResult.Failure -> repositoriesResult
            is UseCaseResult.Success -> {
                runCatchingCancellable {
                    val repositories = repositoriesResult.value
                    val repositoryGroups = preferencesStore.loadRepositoryGroups()
                    val expandedRepositoryIds =
                        preferencesStore.expandedRepositoryIds.filter { repositoryId ->
                            repositories.any { repository -> repository.id.value == repositoryId }
                        }.toSet()
                    val collapsedGroupIds =
                        preferencesStore.collapsedGroupIds.filter { groupId ->
                            repositoryGroups.any { group -> group.id.value == groupId }
                        }.toSet()
                    val selectedRepositoryId =
                        preferencesStore.lastSelectedRepositoryId?.takeIf { selectedId ->
                            repositories.any { repository -> repository.id.value == selectedId }
                        }
                    val kanbanTasksByRepositoryId =
                        repositories.associate { repository ->
                            repository.id.value to preferencesStore.loadKanbanTasks(forRepositoryId = repository.id)
                        }

                    UseCaseResult.Success(
                        value =
                            Output(
                                repositories = repositories,
                                repositoryGroups = repositoryGroups,
                                expandedRepositoryIds = expandedRepositoryIds,
                                collapsedGroupIds = collapsedGroupIds,
                                selectedRepositoryId = selectedRepositoryId,
                                kanbanTasksByRepositoryId = kanbanTasksByRepositoryId,
                                selectedWorktreePath = preferencesStore.lastSelectedWorktreePath,
                                worktreeBasePath = preferencesStore.worktreeBasePath,
                                defaultCopyPatterns = preferencesStore.defaultCopyPatterns,
                                rememberEditorChoice = preferencesStore.rememberEditorChoice,
                                enabledEditorIds = preferencesStore.enabledEditorIds,
                            ),
                    )
                }.fold(
                    onSuccess = { it },
                    onFailure = { throwable ->
                        throwable.toUseCaseFailure()
                    },
                )
            }
        }
    }

    data class Output(
        val repositories: List<Repository>,
        val repositoryGroups: List<RepositoryGroup>,
        val expandedRepositoryIds: Set<String>,
        val collapsedGroupIds: Set<String>,
        val selectedRepositoryId: String?,
        val kanbanTasksByRepositoryId: Map<String, List<KanbanTask>>,
        val selectedWorktreePath: String?,
        val worktreeBasePath: String,
        val defaultCopyPatterns: List<CopyPattern>,
        val rememberEditorChoice: Boolean,
        val enabledEditorIds: Set<String>?,
    )
}
