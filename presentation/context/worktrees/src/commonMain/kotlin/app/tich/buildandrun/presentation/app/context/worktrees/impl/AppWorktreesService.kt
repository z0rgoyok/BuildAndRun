package app.tich.buildandrun.presentation.app.context.worktrees.impl

import app.tich.buildandrun.application.context.repositories.usecase.AppSessionPersistenceUseCase
import app.tich.buildandrun.application.context.repositories.usecase.PersistCreatedWorktreePreferencesUseCase
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.worktrees.usecase.*
import app.tich.buildandrun.presentation.app.AppWorktreesFeature
import app.tich.buildandrun.presentation.app.SuccessState
import app.tich.buildandrun.presentation.app.context.state.MessagesContextState
import app.tich.buildandrun.presentation.app.context.state.RepositoriesContextState
import app.tich.buildandrun.presentation.app.context.state.WorktreesContextState
import app.tich.buildandrun.presentation.app.core.*
import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.resources.Res
import app.tich.buildandrun.resources.loading_refreshing
import app.tich.buildandrun.resources.screen_create_worktree_success
import kotlinx.coroutines.launch

class AppWorktreesService(
    private val executionScope: AppExecutionScope,
    private val loadingRunner: AppLoadingRunner,
    private val stateRefresher: AppStateRefresher,
    private val errorMapper: AppErrorStateMapper,
    private val repositoriesState: RepositoriesContextState,
    private val worktreesState: WorktreesContextState,
    private val messagesState: MessagesContextState,
    private val editorOpening: EditorOpening,
    private val createWorktreeUseCase: CreateWorktreeUseCase,
    private val loadBranchesUseCase: LoadBranchesUseCase,
    private val copyConfiguredFilesUseCase: CopyConfiguredFilesUseCase,
    private val loadRepositoryWorktreesUseCase: LoadRepositoryWorktreesUseCase,
    private val loadWorktreeStatusUseCase: LoadWorktreeStatusUseCase,
    private val appSessionPersistenceUseCase: AppSessionPersistenceUseCase,
    private val persistCreatedWorktreePreferencesUseCase: PersistCreatedWorktreePreferencesUseCase,
) : AppWorktreesFeature, WorktreesOperations {
    override fun onSelectWorktree(worktreePath: String?) {
        worktreesState.selectedWorktreePath = worktreePath
        persistSelection()
        messagesState.clear()
        stateRefresher.publishAll()
    }

    override fun onRefreshSelectedRepository() {
        val repositoryPath = repositoriesState.selectedRepository()?.path ?: return
        messagesState.clear()
        executionScope.scope.launch { stateRefresher.refreshInstalledEditors(editorOpening = editorOpening) }
        loadWorktreesForRepository(path = repositoryPath)
        loadSelectedRepositoryBranches()
    }

    override fun onRefreshWorktreeStatus(worktreePath: String) {
        val normalizedPath = normalizePath(worktreePath)
        if (normalizedPath.isBlank()) {
            return
        }
        if (worktreesState.worktreeStatusLoadingPaths.contains(normalizedPath)) {
            return
        }
        executionScope.scope.launch {
            worktreesState.worktreeStatusLoadingPaths += normalizedPath
            stateRefresher.publishAll()
            when (
                val result =
                    loadWorktreeStatusUseCase.execute(
                        input = LoadWorktreeStatusUseCase.Input(worktreePath = normalizedPath),
                    )
            ) {
                is UseCaseResult.Success -> {
                    worktreesState.worktreeStatusByPath[normalizedPath] = result.value.status
                }

                is UseCaseResult.Failure -> {
                    messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                }
            }
            worktreesState.worktreeStatusLoadingPaths -= normalizedPath
            stateRefresher.publishAll()
        }
    }

    override fun onCreateWorktreeBranchChanged(value: String) {
        val selectedRepositoryPath = repositoriesState.selectedRepository()?.path.orEmpty()
        val normalizedBranch = value.trim()
        val currentWorktreePath = worktreesState.createWorktreeState.worktreePathInput
        val updatedWorktreePath =
            if (currentWorktreePath.isBlank() && normalizedBranch.isNotBlank() && selectedRepositoryPath.isNotBlank()) {
                suggestWorktreePath(repositoryPath = selectedRepositoryPath, branch = normalizedBranch)
            } else {
                currentWorktreePath
            }
        worktreesState.createWorktreeState =
            worktreesState.createWorktreeState.copy(
                branchInput = value,
                worktreePathInput = updatedWorktreePath,
                createdWorktreePath = null,
            )
        messagesState.clear()
        stateRefresher.publishAll()
    }

    override fun onCreateWorktreePathChanged(value: String) {
        worktreesState.createWorktreeState =
            worktreesState.createWorktreeState.copy(
                worktreePathInput = value,
                createdWorktreePath = null,
            )
        messagesState.clear()
        stateRefresher.publishAll()
    }

    override fun onCreateWorktreeBaseBranchChanged(value: String) {
        worktreesState.createWorktreeState = worktreesState.createWorktreeState.copy(baseBranchInput = value)
        messagesState.clear()
        stateRefresher.publishAll()
    }

    override fun onCreateWorktreeCreateBranchChanged(value: Boolean) {
        worktreesState.createWorktreeState = worktreesState.createWorktreeState.copy(createBranch = value)
        messagesState.clear()
        stateRefresher.publishAll()
    }

    override fun onCreateWorktree() {
        if (worktreesState.createWorktreeState.isSubmitting || stateRefresher.isGlobalActive()) {
            return
        }
        val repositoryPath = repositoriesState.selectedRepository()?.path ?: return
        executionScope.scope.launch {
            val repository = repositoriesState.selectedRepository()
            if (repository == null) {
                worktreesState.createWorktreeState = worktreesState.createWorktreeState.copy(isSubmitting = false)
                return@launch
            }
            worktreesState.createWorktreeState =
                worktreesState.createWorktreeState.copy(
                    isSubmitting = true,
                    createdWorktreePath = null,
                )
            messagesState.clear()
            stateRefresher.publishAll()
            when (
                val result =
                    createWorktreeUseCase.execute(
                        input =
                            CreateWorktreeUseCase.Input(
                                repositoryPath = repositoryPath,
                                branch = worktreesState.createWorktreeState.branchInput,
                                worktreePath = worktreesState.createWorktreeState.worktreePathInput,
                                createBranch = worktreesState.createWorktreeState.createBranch,
                                baseBranch = worktreesState.createWorktreeState.baseBranchInput,
                            ),
                    )
            ) {
                is UseCaseResult.Success -> {
                    when (
                        val persistenceResult =
                            persistCreatedWorktreePreferencesUseCase.execute(
                                input =
                                    PersistCreatedWorktreePreferencesUseCase.Input(
                                        repositoryId = repository.id.value,
                                        worktreePath = result.value.createdWorktree.path,
                                        baseBranch = worktreesState.createWorktreeState.baseBranchInput,
                                    ),
                            )
                    ) {
                        is UseCaseResult.Success -> {
                        }

                        is UseCaseResult.Failure -> {
                            messagesState.error = errorMapper.mapFailureToErrorState(persistenceResult.value)
                        }
                    }
                    copyConfiguredFilesUseCase.execute(
                        input =
                            CopyConfiguredFilesUseCase.Input(
                                repositoryPath = repository.path,
                                createdWorktreePath = result.value.createdWorktree.path,
                                repositoryId = repository.id.value,
                            ),
                    )
                    when (
                        val loadResult =
                            loadRepositoryWorktreesUseCase.execute(
                                input = LoadRepositoryWorktreesUseCase.Input(repositoryPath = repositoryPath),
                            )
                    ) {
                        is UseCaseResult.Success -> {
                            worktreesState.worktreesByRepositoryPath[repositoryPath] = loadResult.value.worktrees
                        }

                        is UseCaseResult.Failure -> {
                            messagesState.error = errorMapper.mapFailureToErrorState(loadResult.value)
                        }
                    }
                    worktreesState.selectedWorktreePath = result.value.createdWorktree.path
                    persistSelection()
                    worktreesState.createWorktreeState =
                        worktreesState.createWorktreeState.copy(
                            isSubmitting = false,
                            createdWorktreePath = result.value.createdWorktree.path,
                        )
                    loadSelectedRepositoryBranches()
                    messagesState.success =
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
                    worktreesState.createWorktreeState = worktreesState.createWorktreeState.copy(isSubmitting = false)
                    messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                }
            }
            stateRefresher.publishAll()
        }
    }

    override fun loadWorktreesForRepository(path: String) {
        executionScope.scope.launch {
            loadingRunner.withGlobalLoading(Res.string.loading_refreshing) {
                loadWorktreesForRepositoryInternal(path = path)
            }
        }
    }

    override suspend fun loadWorktreesForRepositoryInternal(path: String) {
        val normalizedPath = normalizePath(path)
        if (normalizedPath.isBlank()) {
            return
        }
        when (
            val result =
                loadRepositoryWorktreesUseCase.execute(
                    input = LoadRepositoryWorktreesUseCase.Input(repositoryPath = normalizedPath),
                )
        ) {
            is UseCaseResult.Success -> {
                worktreesState.worktreesByRepositoryPath[normalizedPath] = result.value.worktrees
                if (repositoriesState.selectedRepository()?.path == normalizedPath && worktreesState.selectedWorktreePath != null) {
                    val stillExists =
                        worktreesState.worktreesByRepositoryPath[normalizedPath]
                            .orEmpty()
                            .any { it.path == worktreesState.selectedWorktreePath }
                    if (!stillExists) {
                        worktreesState.selectedWorktreePath = null
                        persistSelection()
                    }
                }
                worktreesState.worktreesByRepositoryPath[normalizedPath].orEmpty().forEach { worktree ->
                    if (!worktreesState.worktreeStatusByPath.containsKey(worktree.path)) {
                        onRefreshWorktreeStatus(worktreePath = worktree.path)
                    }
                }
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
    }

    private fun loadSelectedRepositoryBranches() {
        val repositoryPath = repositoriesState.selectedRepository()?.path ?: return
        executionScope.scope.launch {
            when (
                val result =
                    loadBranchesUseCase.execute(
                        input =
                            LoadBranchesUseCase.Input(
                                repositoryPath = repositoryPath,
                            ),
                    )
            ) {
                is UseCaseResult.Success -> {
                    stateRefresher.settingsState.branches = result.value.branches
                }

                is UseCaseResult.Failure -> {
                    messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                }
            }
            stateRefresher.publishAll()
        }
    }

    private fun persistSelection() {
        when (
            val result =
                appSessionPersistenceUseCase.execute(
                    input =
                        AppSessionPersistenceUseCase.Input(
                            repositoryId = repositoriesState.selectedRepositoryId,
                            worktreePath = worktreesState.selectedWorktreePath,
                        ),
                )
        ) {
            is UseCaseResult.Success -> {
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
    }
}
