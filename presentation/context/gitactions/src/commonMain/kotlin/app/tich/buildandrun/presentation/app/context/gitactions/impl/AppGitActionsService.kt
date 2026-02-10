package app.tich.buildandrun.presentation.app.context.gitactions.impl

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.port.SystemOpening
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.domain.context.worktrees.model.CompleteWorktreeOptions
import app.tich.buildandrun.domain.shared.error.AppError
import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper
import app.tich.buildandrun.presentation.app.AppGitActionsFeature
import app.tich.buildandrun.presentation.app.SuccessState
import app.tich.buildandrun.presentation.app.context.state.MessagesContextState
import app.tich.buildandrun.presentation.app.context.state.RepositoriesContextState
import app.tich.buildandrun.presentation.app.context.state.SettingsContextState
import app.tich.buildandrun.presentation.app.context.state.WorktreesContextState
import app.tich.buildandrun.presentation.app.context.worktrees.impl.WorktreesOperations
import app.tich.buildandrun.presentation.app.core.AppErrorStateMapper
import app.tich.buildandrun.presentation.app.core.AppExecutionScope
import app.tich.buildandrun.presentation.app.core.AppLoadingRunner
import app.tich.buildandrun.presentation.app.core.AppStateRefresher
import app.tich.buildandrun.resources.*
import kotlinx.coroutines.launch

class AppGitActionsService(
    private val executionScope: AppExecutionScope,
    private val loadingRunner: AppLoadingRunner,
    private val stateRefresher: AppStateRefresher,
    private val errorMapper: AppErrorStateMapper,
    private val repositoriesState: RepositoriesContextState,
    private val worktreesState: WorktreesContextState,
    private val settingsState: SettingsContextState,
    private val messagesState: MessagesContextState,
    private val gitClient: GitClient,
    private val preferencesStore: PreferencesStore,
    private val systemOpening: SystemOpening,
    private val worktreesOperations: WorktreesOperations,
) : AppGitActionsFeature {
    override fun onPush(worktreePath: String) {
        val pair = worktreesState.findWorktreeByPath(path = worktreePath, repositoriesState = repositoriesState) ?: return
        executionScope.scope.launch {
            loadingRunner.withWorktreeLoading(pair.second.path, Res.string.loading_pushing) {
                runCatching {
                    val status = gitClient.getWorktreeStatus(atWorktreePath = pair.second.path)
                    gitClient.push(
                        atWorktreePath = pair.second.path,
                        setUpstream = !status.hasRemote,
                    )
                    gitClient.getWorktreeStatus(atWorktreePath = pair.second.path)
                }.onSuccess { status ->
                    worktreesState.worktreeStatusByPath[pair.second.path] = status
                    messagesState.success = SuccessState(message = "Push completed")
                }.onFailure { throwable ->
                    messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
                }
            }
        }
    }

    override fun onPull(worktreePath: String) {
        val pair = worktreesState.findWorktreeByPath(path = worktreePath, repositoriesState = repositoriesState) ?: return
        executionScope.scope.launch {
            loadingRunner.withWorktreeLoading(pair.second.path, Res.string.loading_pulling) {
                runCatching {
                    gitClient.pull(atWorktreePath = pair.second.path)
                    gitClient.getWorktreeStatus(atWorktreePath = pair.second.path)
                }.onSuccess { status ->
                    worktreesState.worktreeStatusByPath[pair.second.path] = status
                    messagesState.success = SuccessState(message = "Pull completed")
                }.onFailure { throwable ->
                    messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
                }
            }
        }
    }

    override fun onCreatePullRequest(
        worktreePath: String,
        title: String,
        body: String,
        baseBranch: String?,
    ) {
        val pair = worktreesState.findWorktreeByPath(path = worktreePath, repositoriesState = repositoriesState) ?: return
        executionScope.scope.launch {
            loadingRunner.withWorktreeLoading(pair.second.path, Res.string.loading_creating_pr) {
                runCatching {
                    val status = gitClient.getWorktreeStatus(atWorktreePath = pair.second.path)
                    if (status.hasUnpushedCommits || !status.hasRemote) {
                        gitClient.push(
                            atWorktreePath = pair.second.path,
                            setUpstream = !status.hasRemote,
                        )
                    }
                    val prUrl =
                        gitClient.createPR(
                            atWorktreePath = pair.second.path,
                            title = title.ifBlank { pair.second.branch },
                            body = body,
                            baseBranch = baseBranch,
                        )
                    val updatedStatus = gitClient.getWorktreeStatus(atWorktreePath = pair.second.path)
                    Pair(prUrl, updatedStatus)
                }.onSuccess { result ->
                    worktreesState.worktreeStatusByPath[pair.second.path] = result.second
                    systemOpening.openURL(url = result.first)
                    messagesState.success = SuccessState(message = "Pull request created")
                }.onFailure { throwable ->
                    messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
                }
            }
        }
    }

    override fun onOpenPullRequest(worktreePath: String) {
        val pair = worktreesState.findWorktreeByPath(path = worktreePath, repositoriesState = repositoriesState) ?: return
        executionScope.scope.launch {
            runCatching {
                val status = gitClient.getWorktreeStatus(atWorktreePath = pair.second.path)
                worktreesState.worktreeStatusByPath[pair.second.path] = status
                status.prStatus?.url
            }.onSuccess { url ->
                if (url != null) {
                    systemOpening.openURL(url = url)
                }
            }.onFailure { throwable ->
                messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
                stateRefresher.publishAll()
            }
        }
    }

    override fun onLockWorktree(worktreePath: String) {
        val pair = worktreesState.findWorktreeByPath(path = worktreePath, repositoriesState = repositoriesState) ?: return
        executionScope.scope.launch {
            loadingRunner.withWorktreeLoading(pair.second.path, Res.string.loading_locking) {
                runCatching {
                    gitClient.lockWorktree(
                        atRepoPath = pair.first.path,
                        worktreePath = pair.second.path,
                        reason = null,
                    )
                    worktreesOperations.loadWorktreesForRepositoryInternal(path = pair.first.path)
                }.onSuccess {
                    messagesState.success = SuccessState(message = "Worktree locked")
                }.onFailure { throwable ->
                    messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
                }
            }
        }
    }

    override fun onUnlockWorktree(worktreePath: String) {
        val pair = worktreesState.findWorktreeByPath(path = worktreePath, repositoriesState = repositoriesState) ?: return
        executionScope.scope.launch {
            loadingRunner.withWorktreeLoading(pair.second.path, Res.string.loading_unlocking) {
                runCatching {
                    gitClient.unlockWorktree(
                        atRepoPath = pair.first.path,
                        worktreePath = pair.second.path,
                    )
                    worktreesOperations.loadWorktreesForRepositoryInternal(path = pair.first.path)
                }.onSuccess {
                    messagesState.success = SuccessState(message = "Worktree unlocked")
                }.onFailure { throwable ->
                    messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
                }
            }
        }
    }

    override fun onRemoveWorktree(
        worktreePath: String,
        force: Boolean,
        deleteBranch: Boolean,
    ) {
        val pair = worktreesState.findWorktreeByPath(path = worktreePath, repositoriesState = repositoriesState) ?: return
        executionScope.scope.launch {
            loadingRunner.withWorktreeLoading(pair.second.path, Res.string.loading_removing_worktree) {
                runCatching {
                    if (pair.second.isMain) {
                        throw AppError.CannotRemoveMainWorktree()
                    }
                    gitClient.removeWorktree(
                        atRepoPath = pair.first.path,
                        worktreePath = pair.second.path,
                        force = force,
                    )
                    preferencesStore.removeWorktreeBaseBranch(forWorktreePath = pair.second.path)
                    if (deleteBranch && pair.second.branch.isNotBlank() && pair.second.branch != "detached HEAD") {
                        gitClient.deleteBranch(
                            atRepoPath = pair.first.path,
                            branch = pair.second.branch,
                            force = force,
                        )
                    }
                    if (worktreesState.selectedWorktreePath == pair.second.path) {
                        worktreesState.selectedWorktreePath = null
                        stateRefresher.persistSelection()
                    }
                    worktreesOperations.loadWorktreesForRepositoryInternal(path = pair.first.path)
                    settingsState.branches = gitClient.listBranches(atRepoPath = pair.first.path)
                }.onSuccess {
                    messagesState.success = SuccessState(message = "Worktree removed")
                }.onFailure { throwable ->
                    messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
                }
            }
        }
    }

    override fun onCompleteWorktree(
        worktreePath: String,
        targetBranch: String,
        mergeIntoTarget: Boolean,
        pullTargetFirst: Boolean,
        deleteLocalBranch: Boolean,
        deleteRemoteBranch: Boolean,
        force: Boolean,
    ) {
        val pair = worktreesState.findWorktreeByPath(path = worktreePath, repositoriesState = repositoriesState) ?: return
        val options =
            CompleteWorktreeOptions(
                targetBranch = targetBranch,
                mergeIntoTarget = mergeIntoTarget,
                pullTargetFirst = pullTargetFirst,
                deleteLocalBranch = deleteLocalBranch,
                deleteRemoteBranch = deleteRemoteBranch,
                force = force,
            )
        executionScope.scope.launch {
            loadingRunner.withWorktreeLoading(pair.second.path, Res.string.loading_completing) {
                runCatching {
                    if (pair.second.isMain) {
                        throw AppError.CannotRemoveMainWorktree()
                    }
                    if (options.mergeIntoTarget) {
                        gitClient.mergeBranch(
                            atRepoPath = pair.first.path,
                            source = pair.second.branch,
                            intoTarget = options.targetBranch,
                        )
                    }
                    if (options.pullTargetFirst) {
                        val repoWorktrees = gitClient.listWorktrees(atRepoPath = pair.first.path)
                        val targetWorktree = repoWorktrees.firstOrNull { it.branch == options.targetBranch }
                        if (targetWorktree != null) {
                            gitClient.pull(atWorktreePath = targetWorktree.path)
                        }
                    }
                    gitClient.removeWorktree(
                        atRepoPath = pair.first.path,
                        worktreePath = pair.second.path,
                        force = options.force,
                    )
                    preferencesStore.removeWorktreeBaseBranch(forWorktreePath = pair.second.path)
                    if (options.deleteLocalBranch && pair.second.branch.isNotBlank() && pair.second.branch != "detached HEAD") {
                        gitClient.deleteBranch(
                            atRepoPath = pair.first.path,
                            branch = pair.second.branch,
                            force = options.force,
                        )
                    }
                    if (options.deleteRemoteBranch && pair.second.branch.isNotBlank()) {
                        gitClient.deleteRemoteBranch(
                            atRepoPath = pair.first.path,
                            branch = pair.second.branch,
                        )
                    }
                    if (worktreesState.selectedWorktreePath == pair.second.path) {
                        worktreesState.selectedWorktreePath = null
                        stateRefresher.persistSelection()
                    }
                    worktreesOperations.loadWorktreesForRepositoryInternal(path = pair.first.path)
                    settingsState.branches = gitClient.listBranches(atRepoPath = pair.first.path)
                }.onSuccess {
                    messagesState.success = SuccessState(message = "Worktree completed")
                }.onFailure { throwable ->
                    messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
                }
            }
        }
    }

    override fun onLoadHasRemoteBranch(worktreePath: String) {
        val pair = worktreesState.findWorktreeByPath(path = worktreePath, repositoriesState = repositoriesState) ?: return
        executionScope.scope.launch {
            runCatching {
                gitClient.hasRemoteBranch(
                    atRepoPath = pair.first.path,
                    branch = pair.second.branch,
                )
            }.onSuccess { hasRemote ->
                worktreesState.hasRemoteBranchByWorktreePath[pair.second.path] = hasRemote
                stateRefresher.publishAll()
            }.onFailure { throwable ->
                messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
                stateRefresher.publishAll()
            }
        }
    }

    override fun onPruneWorktrees() {
        val repositoryPath = repositoriesState.selectedRepository()?.path ?: return
        executionScope.scope.launch {
            loadingRunner.withGlobalLoading(Res.string.loading_pruning) {
                runCatching {
                    gitClient.pruneWorktrees(atRepoPath = repositoryPath)
                    worktreesOperations.loadWorktreesForRepositoryInternal(path = repositoryPath)
                }.onSuccess {
                    messagesState.success = SuccessState(message = "Worktrees pruned")
                }.onFailure { throwable ->
                    messagesState.error = errorMapper.mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
                }
            }
        }
    }
}
