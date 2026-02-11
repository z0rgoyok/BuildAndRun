package app.tich.buildandrun.presentation.app.context.gitactions.impl

import app.tich.buildandrun.application.context.repositories.usecase.AppSessionPersistenceUseCase
import app.tich.buildandrun.application.context.shared.usecase.OpenUrlUseCase
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.worktrees.usecase.*
import app.tich.buildandrun.domain.context.worktrees.model.CompleteWorktreeOptions
import app.tich.buildandrun.presentation.app.AppGitActionsFeature
import app.tich.buildandrun.presentation.app.SuccessState
import app.tich.buildandrun.presentation.app.context.state.MessagesContextState
import app.tich.buildandrun.presentation.app.context.state.RepositoriesContextState
import app.tich.buildandrun.presentation.app.context.state.SettingsContextState
import app.tich.buildandrun.presentation.app.context.state.WorktreesContextState
import app.tich.buildandrun.presentation.app.core.*
import app.tich.buildandrun.presentation.i18n.UiText
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
    private val openUrlUseCase: OpenUrlUseCase,
    private val appSessionPersistenceUseCase: AppSessionPersistenceUseCase,
    private val pushWorktreeUseCase: PushWorktreeUseCase,
    private val pullWorktreeUseCase: PullWorktreeUseCase,
    private val createPullRequestUseCase: CreatePullRequestUseCase,
    private val loadPullRequestUrlUseCase: LoadPullRequestUrlUseCase,
    private val lockWorktreeUseCase: LockWorktreeUseCase,
    private val unlockWorktreeUseCase: UnlockWorktreeUseCase,
    private val removeWorktreeUseCase: RemoveWorktreeUseCase,
    private val completeWorktreeUseCase: CompleteWorktreeUseCase,
    private val loadHasRemoteBranchUseCase: LoadHasRemoteBranchUseCase,
    private val pruneWorktreesUseCase: PruneWorktreesUseCase,
) : AppGitActionsFeature {
    override fun onPush(worktreePath: String) {
        val pair = worktreesState.findWorktreeByPath(path = worktreePath, repositoriesState = repositoriesState) ?: return
        executionScope.scope.launch {
            loadingRunner.withWorktreeLoading(pair.second.path, Res.string.loading_pushing) {
                when (
                    val result =
                        pushWorktreeUseCase.execute(
                            input = PushWorktreeUseCase.Input(worktreePath = pair.second.path),
                        )
                ) {
                    is UseCaseResult.Success -> {
                        worktreesState.worktreeStatusByPath[pair.second.path] = result.value.status
                        messagesState.success = success(Res.string.screen_git_push_success)
                    }
                    is UseCaseResult.Failure -> {
                        messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                    }
                }
            }
        }
    }

    override fun onPull(worktreePath: String) {
        val pair = worktreesState.findWorktreeByPath(path = worktreePath, repositoriesState = repositoriesState) ?: return
        executionScope.scope.launch {
            loadingRunner.withWorktreeLoading(pair.second.path, Res.string.loading_pulling) {
                when (
                    val result =
                        pullWorktreeUseCase.execute(
                            input = PullWorktreeUseCase.Input(worktreePath = pair.second.path),
                        )
                ) {
                    is UseCaseResult.Success -> {
                        worktreesState.worktreeStatusByPath[pair.second.path] = result.value.status
                        messagesState.success = success(Res.string.screen_git_pull_success)
                    }
                    is UseCaseResult.Failure -> {
                        messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                    }
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
                when (
                    val result =
                        createPullRequestUseCase.execute(
                            input =
                                CreatePullRequestUseCase.Input(
                                    worktree = pair.second,
                                    title = title,
                                    body = body,
                                    baseBranch = baseBranch,
                                ),
                        )
                ) {
                    is UseCaseResult.Success -> {
                        worktreesState.worktreeStatusByPath[pair.second.path] = result.value.status
                        if (openGitActionUrl(
                                openUrlUseCase = openUrlUseCase,
                                url = result.value.pullRequestUrl,
                                onFailure = { failure -> messagesState.error = errorMapper.mapFailureToErrorState(failure) },
                            )
                        ) {
                            messagesState.success = success(Res.string.screen_git_pr_created_success)
                        }
                    }
                    is UseCaseResult.Failure -> {
                        messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                    }
                }
            }
        }
    }

    override fun onOpenPullRequest(worktreePath: String) {
        val pair = worktreesState.findWorktreeByPath(path = worktreePath, repositoriesState = repositoriesState) ?: return
        executionScope.scope.launch {
            when (
                val result =
                    loadPullRequestUrlUseCase.execute(
                        input = LoadPullRequestUrlUseCase.Input(worktreePath = pair.second.path),
                    )
            ) {
                is UseCaseResult.Success -> {
                    worktreesState.worktreeStatusByPath[pair.second.path] = result.value.status
                    result.value.pullRequestUrl?.let { url ->
                        openGitActionUrl(
                            openUrlUseCase = openUrlUseCase,
                            url = url,
                            onFailure = { failure ->
                                messagesState.error = errorMapper.mapFailureToErrorState(failure)
                                stateRefresher.publishAll()
                            },
                        )
                    }
                }
                is UseCaseResult.Failure -> {
                    messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                    stateRefresher.publishAll()
                }
            }
        }
    }

    override fun onLockWorktree(worktreePath: String) {
        val pair = worktreesState.findWorktreeByPath(path = worktreePath, repositoriesState = repositoriesState) ?: return
        executionScope.scope.launch {
            loadingRunner.withWorktreeLoading(pair.second.path, Res.string.loading_locking) {
                when (
                    val result =
                        lockWorktreeUseCase.execute(
                            input =
                                LockWorktreeUseCase.Input(
                                    repositoryPath = pair.first.path,
                                    worktreePath = pair.second.path,
                                ),
                        )
                ) {
                    is UseCaseResult.Success -> {
                        worktreesState.worktreesByRepositoryPath[pair.first.path] = result.value.worktrees
                        messagesState.success = success(Res.string.screen_git_worktree_locked_success)
                    }
                    is UseCaseResult.Failure -> {
                        messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                    }
                }
            }
        }
    }

    override fun onUnlockWorktree(worktreePath: String) {
        val pair = worktreesState.findWorktreeByPath(path = worktreePath, repositoriesState = repositoriesState) ?: return
        executionScope.scope.launch {
            loadingRunner.withWorktreeLoading(pair.second.path, Res.string.loading_unlocking) {
                when (
                    val result =
                        unlockWorktreeUseCase.execute(
                            input =
                                UnlockWorktreeUseCase.Input(
                                    repositoryPath = pair.first.path,
                                    worktreePath = pair.second.path,
                                ),
                        )
                ) {
                    is UseCaseResult.Success -> {
                        worktreesState.worktreesByRepositoryPath[pair.first.path] = result.value.worktrees
                        messagesState.success = success(Res.string.screen_git_worktree_unlocked_success)
                    }
                    is UseCaseResult.Failure -> {
                        messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                    }
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
                when (
                    val result =
                        removeWorktreeUseCase.execute(
                            input =
                                RemoveWorktreeUseCase.Input(
                                    repositoryPath = pair.first.path,
                                    worktree = pair.second,
                                    force = force,
                                    deleteBranch = deleteBranch,
                                ),
                        )
                ) {
                    is UseCaseResult.Success -> {
                        worktreesState.worktreesByRepositoryPath[pair.first.path] = result.value.worktrees
                        settingsState.branches = result.value.branches
                        if (worktreesState.selectedWorktreePath == pair.second.path) {
                            worktreesState.selectedWorktreePath = null
                            persistSelection()
                        }
                        messagesState.success = success(Res.string.screen_git_worktree_removed_success)
                    }
                    is UseCaseResult.Failure -> {
                        messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                    }
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
                when (
                    val result =
                        completeWorktreeUseCase.execute(
                            input =
                                CompleteWorktreeUseCase.Input(
                                    repositoryPath = pair.first.path,
                                    worktree = pair.second,
                                    options = options,
                                ),
                        )
                ) {
                    is UseCaseResult.Success -> {
                        settingsState.branches = result.value.branches
                        worktreesState.worktreesByRepositoryPath[pair.first.path] = result.value.worktrees
                        if (pair.second.path == worktreesState.selectedWorktreePath) {
                            worktreesState.selectedWorktreePath = null
                            persistSelection()
                        }
                        messagesState.success = success(Res.string.screen_git_worktree_completed_success)
                    }
                    is UseCaseResult.Failure -> {
                        messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                    }
                }
            }
        }
    }

    override fun onLoadHasRemoteBranch(worktreePath: String) {
        val pair = worktreesState.findWorktreeByPath(path = worktreePath, repositoriesState = repositoriesState) ?: return
        executionScope.scope.launch {
            when (
                val result =
                    loadHasRemoteBranchUseCase.execute(
                        input =
                            LoadHasRemoteBranchUseCase.Input(
                                repositoryPath = pair.first.path,
                                branch = pair.second.branch,
                            ),
                    )
            ) {
                is UseCaseResult.Success -> {
                    worktreesState.hasRemoteBranchByWorktreePath[pair.second.path] = result.value.hasRemoteBranch
                    stateRefresher.publishAll()
                }
                is UseCaseResult.Failure -> {
                    messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                    stateRefresher.publishAll()
                }
            }
        }
    }

    override fun onPruneWorktrees() {
        val repositoryPath = repositoriesState.selectedRepository()?.path ?: return
        executionScope.scope.launch {
            loadingRunner.withGlobalLoading(Res.string.loading_pruning) {
                when (
                    val result =
                        pruneWorktreesUseCase.execute(
                            input = PruneWorktreesUseCase.Input(repositoryPath = repositoryPath),
                        )
                ) {
                    is UseCaseResult.Success -> {
                        worktreesState.worktreesByRepositoryPath[repositoryPath] = result.value.worktrees
                        messagesState.success = success(Res.string.screen_git_worktrees_pruned_success)
                    }
                    is UseCaseResult.Failure -> {
                        messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                    }
                }
            }
        }
    }

    private fun persistSelection() =
        persistGitSelection(
            appSessionPersistenceUseCase = appSessionPersistenceUseCase,
            repositoryId = repositoriesState.selectedRepositoryId,
            worktreePath = worktreesState.selectedWorktreePath,
            onFailure = { failure -> messagesState.error = errorMapper.mapFailureToErrorState(failure) },
        )

    private fun success(resource: org.jetbrains.compose.resources.StringResource): SuccessState =
        SuccessState(message = resolveText(text = UiText(resource = resource)))
}
