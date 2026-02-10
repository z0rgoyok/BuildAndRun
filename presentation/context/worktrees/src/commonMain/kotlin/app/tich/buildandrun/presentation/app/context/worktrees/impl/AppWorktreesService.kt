package app.tich.buildandrun.presentation.app.context.worktrees.impl

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.application.context.worktrees.usecase.CopyConfiguredFilesUseCase
import app.tich.buildandrun.application.context.worktrees.usecase.CreateWorktreeUseCase
import app.tich.buildandrun.application.context.worktrees.usecase.LoadBranchesUseCase
import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper
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
    private val gitClient: GitClient,
    private val preferencesStore: PreferencesStore,
    private val editorOpening: EditorOpening,
    private val createWorktreeUseCase: CreateWorktreeUseCase,
    private val loadBranchesUseCase: LoadBranchesUseCase,
    private val copyConfiguredFilesUseCase: CopyConfiguredFilesUseCase,
) : AppWorktreesFeature, WorktreesOperations {
    override fun onSelectWorktree(worktreePath: String?) {
        worktreesState.selectedWorktreePath = worktreePath
        stateRefresher.persistSelection()
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
            runCatching {
                gitClient.getWorktreeStatus(atWorktreePath = normalizedPath)
            }.onSuccess { status ->
                worktreesState.worktreeStatusByPath[normalizedPath] = status
            }.onFailure { throwable ->
                val domainFailure = DomainFailureMapper.fromThrowable(throwable)
                messagesState.error = errorMapper.mapFailureToErrorState(domainFailure)
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
                    val preferredBaseBranch = worktreesState.createWorktreeState.baseBranchInput.trim().ifBlank { null }
                    preferredBaseBranch?.let { baseBranch ->
                        preferencesStore.setPreferredBaseBranch(
                            branch = baseBranch,
                            forRepositoryId = repository.id,
                        )
                        preferencesStore.setWorktreeBaseBranch(
                            branch = baseBranch,
                            forWorktreePath = result.value.createdWorktree.path,
                        )
                    }
                    copyConfiguredFilesUseCase.execute(
                        input =
                            CopyConfiguredFilesUseCase.Input(
                                repositoryPath = repository.path,
                                createdWorktreePath = result.value.createdWorktree.path,
                                repositoryId = repository.id.value,
                            ),
                    )
                    val worktrees = gitClient.listWorktrees(atRepoPath = repositoryPath)
                    worktreesState.worktreesByRepositoryPath[repositoryPath] = worktrees
                    worktreesState.selectedWorktreePath = result.value.createdWorktree.path
                    stateRefresher.persistSelection()
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
        runCatching {
            gitClient.listWorktrees(atRepoPath = normalizedPath)
        }.onSuccess { worktrees ->
            worktreesState.worktreesByRepositoryPath[normalizedPath] =
                worktrees.map { worktree ->
                    val baseBranch =
                        preferencesStore.worktreeBaseBranch(
                            forWorktreePath = worktree.path,
                        )
                    worktree.withBaseBranch(baseBranch = baseBranch)
                }
            if (repositoriesState.selectedRepository()?.path == normalizedPath && worktreesState.selectedWorktreePath != null) {
                val stillExists =
                    worktreesState.worktreesByRepositoryPath[normalizedPath]
                        .orEmpty()
                        .any { it.path == worktreesState.selectedWorktreePath }
                if (!stillExists) {
                    worktreesState.selectedWorktreePath = null
                    stateRefresher.persistSelection()
                }
            }
            worktreesState.worktreesByRepositoryPath[normalizedPath].orEmpty().forEach { worktree ->
                if (!worktreesState.worktreeStatusByPath.containsKey(worktree.path)) {
                    onRefreshWorktreeStatus(worktreePath = worktree.path)
                }
            }
        }.onFailure { throwable ->
            val domainFailure = DomainFailureMapper.fromThrowable(throwable)
            messagesState.error = errorMapper.mapFailureToErrorState(domainFailure)
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
}
