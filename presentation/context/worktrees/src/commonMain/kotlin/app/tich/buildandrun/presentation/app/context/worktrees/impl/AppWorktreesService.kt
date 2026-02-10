package app.tich.buildandrun.presentation.app.context.worktrees.impl

import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.worktrees.usecase.CreateWorktreeUseCase
import app.tich.buildandrun.presentation.app.AppWorktreesFeature
import app.tich.buildandrun.presentation.app.SuccessState
import app.tich.buildandrun.presentation.app.context.worktrees.impl.usecase.CopyConfiguredFilesUseCase
import app.tich.buildandrun.presentation.app.context.worktrees.impl.usecase.LoadSelectedRepositoryBranchesUseCase
import app.tich.buildandrun.presentation.app.core.*
import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.resources.Res
import app.tich.buildandrun.resources.screen_create_worktree_success
import kotlinx.coroutines.launch

class AppWorktreesService(
    private val runtime: AppWiring,
    private val loadSelectedRepositoryBranchesUseCase: LoadSelectedRepositoryBranchesUseCase,
    private val copyConfiguredFilesUseCase: CopyConfiguredFilesUseCase,
) : AppWorktreesFeature {
    override fun onSelectWorktree(worktreePath: String?) {
        runtime.worktreesState.selectedWorktreePath = worktreePath
        runtime.persistSelection()
        runtime.clearMessages()
        runtime.publishState()
    }

    override fun onRefreshSelectedRepository() {
        val repositoryPath = runtime.selectedRepository()?.path ?: return
        runtime.clearMessages()
        runtime.scope.launch { runtime.refreshInstalledEditors() }
        runtime.loadWorktreesForRepository(path = repositoryPath)
        loadSelectedRepositoryBranchesUseCase.execute()
    }

    override fun onRefreshWorktreeStatus(worktreePath: String) {
        runtime.onRefreshWorktreeStatus(worktreePath = worktreePath)
    }

    override fun onCreateWorktreeBranchChanged(value: String) {
        val selectedRepositoryPath = runtime.selectedRepository()?.path.orEmpty()
        val normalizedBranch = value.trim()
        val currentWorktreePath = runtime.worktreesState.createWorktreeState.worktreePathInput
        val updatedWorktreePath =
            if (currentWorktreePath.isBlank() && normalizedBranch.isNotBlank() && selectedRepositoryPath.isNotBlank()) {
                suggestWorktreePath(repositoryPath = selectedRepositoryPath, branch = normalizedBranch)
            } else {
                currentWorktreePath
            }
        runtime.worktreesState.createWorktreeState =
            runtime.worktreesState.createWorktreeState.copy(
                branchInput = value,
                worktreePathInput = updatedWorktreePath,
                createdWorktreePath = null,
            )
        runtime.clearMessages()
        runtime.publishState()
    }

    override fun onCreateWorktreePathChanged(value: String) {
        runtime.worktreesState.createWorktreeState =
            runtime.worktreesState.createWorktreeState.copy(
                worktreePathInput = value,
                createdWorktreePath = null,
            )
        runtime.clearMessages()
        runtime.publishState()
    }

    override fun onCreateWorktreeBaseBranchChanged(value: String) {
        runtime.worktreesState.createWorktreeState = runtime.worktreesState.createWorktreeState.copy(baseBranchInput = value)
        runtime.clearMessages()
        runtime.publishState()
    }

    override fun onCreateWorktreeCreateBranchChanged(value: Boolean) {
        runtime.worktreesState.createWorktreeState = runtime.worktreesState.createWorktreeState.copy(createBranch = value)
        runtime.clearMessages()
        runtime.publishState()
    }

    override fun onCreateWorktree() {
        if (runtime.worktreesState.createWorktreeState.isSubmitting || runtime.activityCenter.isGlobalActive) {
            return
        }
        val repositoryPath = runtime.selectedRepository()?.path ?: return
        runtime.scope.launch {
            val repository = runtime.selectedRepository()
            if (repository == null) {
                runtime.worktreesState.createWorktreeState = runtime.worktreesState.createWorktreeState.copy(isSubmitting = false)
                return@launch
            }
            runtime.worktreesState.createWorktreeState =
                runtime.worktreesState.createWorktreeState.copy(
                    isSubmitting = true,
                    createdWorktreePath = null,
                )
            runtime.clearMessages()
            runtime.publishState()
            when (
                val result =
                    runtime.graph.createWorktreeUseCase.execute(
                        input =
                            CreateWorktreeUseCase.Input(
                                repositoryPath = repositoryPath,
                                branch = runtime.worktreesState.createWorktreeState.branchInput,
                                worktreePath = runtime.worktreesState.createWorktreeState.worktreePathInput,
                                createBranch = runtime.worktreesState.createWorktreeState.createBranch,
                                baseBranch = runtime.worktreesState.createWorktreeState.baseBranchInput,
                            ),
                    )
            ) {
                is UseCaseResult.Success -> {
                    val preferredBaseBranch = runtime.worktreesState.createWorktreeState.baseBranchInput.trim().ifBlank { null }
                    preferredBaseBranch?.let { baseBranch ->
                        runtime.graph.preferencesStore.setPreferredBaseBranch(
                            branch = baseBranch,
                            forRepositoryId = repository.id,
                        )
                        runtime.graph.preferencesStore.setWorktreeBaseBranch(
                            branch = baseBranch,
                            forWorktreePath = result.value.createdWorktree.path,
                        )
                    }
                    copyConfiguredFilesUseCase.execute(
                        repositoryPath = repository.path,
                        createdWorktreePath = result.value.createdWorktree.path,
                        repositoryId = repository.id.value,
                    )
                    val worktrees = runtime.graph.gitClient.listWorktrees(atRepoPath = repositoryPath)
                    runtime.worktreesState.worktreesByRepositoryPath[repositoryPath] = worktrees
                    runtime.worktreesState.selectedWorktreePath = result.value.createdWorktree.path
                    runtime.persistSelection()
                    runtime.worktreesState.createWorktreeState =
                        runtime.worktreesState.createWorktreeState.copy(
                            isSubmitting = false,
                            createdWorktreePath = result.value.createdWorktree.path,
                        )
                    loadSelectedRepositoryBranchesUseCase.execute()
                    runtime.messagesState.success =
                        SuccessState(
                            message =
                                resolveText(
                                    text =
                                        UiText(
                                            resource = Res.string.screen_create_worktree_success,
                                            args = listOf(result.value.createdWorktree.name),
                                        ),
                                ),
                        )
                }

                is UseCaseResult.Failure -> {
                    runtime.worktreesState.createWorktreeState = runtime.worktreesState.createWorktreeState.copy(isSubmitting = false)
                    runtime.messagesState.error = runtime.mapFailureToErrorState(result.value)
                }
            }
            runtime.publishState()
        }
    }
}
