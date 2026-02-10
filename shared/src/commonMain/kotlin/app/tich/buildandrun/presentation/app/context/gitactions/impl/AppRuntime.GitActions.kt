package app.tich.buildandrun.presentation.app.context.gitactions.impl

import app.tich.buildandrun.domain.context.worktrees.model.CompleteWorktreeOptions
import app.tich.buildandrun.domain.shared.error.AppError
import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper
import app.tich.buildandrun.presentation.app.AppStore
import app.tich.buildandrun.presentation.app.context.worktrees.impl.loadWorktreesForRepositoryInternal
import app.tich.buildandrun.presentation.app.core.*
import app.tich.buildandrun.resources.*
import kotlinx.coroutines.launch

internal fun AppRuntime.onPush(worktreePath: String) {
    val pair = findWorktreeByPath(path = worktreePath) ?: return
    scope.launch {
        withWorktreeLoading(pair.second.path, Res.string.loading_pushing) {
            runCatching {
                val status = graph.gitClient.getWorktreeStatus(atWorktreePath = pair.second.path)
                graph.gitClient.push(
                    atWorktreePath = pair.second.path,
                    setUpstream = !status.hasRemote,
                )
                graph.gitClient.getWorktreeStatus(atWorktreePath = pair.second.path)
            }.onSuccess { status ->
                worktreesState.worktreeStatusByPath[pair.second.path] = status
                messagesState.success = AppStore.SuccessState(message = "Push completed")
            }.onFailure { throwable ->
                messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            }
        }
    }
}

internal fun AppRuntime.onPull(worktreePath: String) {
    val pair = findWorktreeByPath(path = worktreePath) ?: return
    scope.launch {
        withWorktreeLoading(pair.second.path, Res.string.loading_pulling) {
            runCatching {
                graph.gitClient.pull(atWorktreePath = pair.second.path)
                graph.gitClient.getWorktreeStatus(atWorktreePath = pair.second.path)
            }.onSuccess { status ->
                worktreesState.worktreeStatusByPath[pair.second.path] = status
                messagesState.success = AppStore.SuccessState(message = "Pull completed")
            }.onFailure { throwable ->
                messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            }
        }
    }
}

internal fun AppRuntime.onCreatePullRequest(
    worktreePath: String,
    title: String,
    body: String,
    baseBranch: String?,
) {
    val pair = findWorktreeByPath(path = worktreePath) ?: return
    scope.launch {
        withWorktreeLoading(pair.second.path, Res.string.loading_creating_pr) {
            runCatching {
                val status = graph.gitClient.getWorktreeStatus(atWorktreePath = pair.second.path)
                if (status.hasUnpushedCommits || !status.hasRemote) {
                    graph.gitClient.push(
                        atWorktreePath = pair.second.path,
                        setUpstream = !status.hasRemote,
                    )
                }
                val prUrl =
                    graph.gitClient.createPR(
                        atWorktreePath = pair.second.path,
                        title = title.ifBlank { pair.second.branch },
                        body = body,
                        baseBranch = baseBranch,
                    )
                val updatedStatus = graph.gitClient.getWorktreeStatus(atWorktreePath = pair.second.path)
                Pair(prUrl, updatedStatus)
            }.onSuccess { result ->
                worktreesState.worktreeStatusByPath[pair.second.path] = result.second
                graph.systemOpening.openURL(url = result.first)
                messagesState.success = AppStore.SuccessState(message = "Pull request created")
            }.onFailure { throwable ->
                messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            }
        }
    }
}

internal fun AppRuntime.onOpenPullRequest(worktreePath: String) {
    val pair = findWorktreeByPath(path = worktreePath) ?: return
    scope.launch {
        runCatching {
            val status = graph.gitClient.getWorktreeStatus(atWorktreePath = pair.second.path)
            worktreesState.worktreeStatusByPath[pair.second.path] = status
            status.prStatus?.url
        }.onSuccess { url ->
            if (url != null) {
                graph.systemOpening.openURL(url = url)
            }
        }.onFailure { throwable ->
            messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            publishState()
        }
    }
}

internal fun AppRuntime.onLockWorktree(worktreePath: String) {
    val pair = findWorktreeByPath(path = worktreePath) ?: return
    scope.launch {
        withWorktreeLoading(pair.second.path, Res.string.loading_locking) {
            runCatching {
                graph.gitClient.lockWorktree(
                    atRepoPath = pair.first.path,
                    worktreePath = pair.second.path,
                    reason = null,
                )
                loadWorktreesForRepositoryInternal(path = pair.first.path)
            }.onSuccess {
                messagesState.success = AppStore.SuccessState(message = "Worktree locked")
            }.onFailure { throwable ->
                messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            }
        }
    }
}

internal fun AppRuntime.onUnlockWorktree(worktreePath: String) {
    val pair = findWorktreeByPath(path = worktreePath) ?: return
    scope.launch {
        withWorktreeLoading(pair.second.path, Res.string.loading_unlocking) {
            runCatching {
                graph.gitClient.unlockWorktree(
                    atRepoPath = pair.first.path,
                    worktreePath = pair.second.path,
                )
                loadWorktreesForRepositoryInternal(path = pair.first.path)
            }.onSuccess {
                messagesState.success = AppStore.SuccessState(message = "Worktree unlocked")
            }.onFailure { throwable ->
                messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            }
        }
    }
}

internal fun AppRuntime.onRemoveWorktree(
    worktreePath: String,
    force: Boolean,
    deleteBranch: Boolean,
) {
    val pair = findWorktreeByPath(path = worktreePath) ?: return
    scope.launch {
        withWorktreeLoading(pair.second.path, Res.string.loading_removing_worktree) {
            runCatching {
                if (pair.second.isMain) {
                    throw AppError.CannotRemoveMainWorktree()
                }
                graph.gitClient.removeWorktree(
                    atRepoPath = pair.first.path,
                    worktreePath = pair.second.path,
                    force = force,
                )
                graph.preferencesStore.removeWorktreeBaseBranch(forWorktreePath = pair.second.path)
                if (deleteBranch && pair.second.branch.isNotBlank() && pair.second.branch != "detached HEAD") {
                    graph.gitClient.deleteBranch(
                        atRepoPath = pair.first.path,
                        branch = pair.second.branch,
                        force = force,
                    )
                }
                if (worktreesState.selectedWorktreePath == pair.second.path) {
                    worktreesState.selectedWorktreePath = null
                    persistSelection()
                }
                loadWorktreesForRepositoryInternal(path = pair.first.path)
                settingsState.branches = graph.gitClient.listBranches(atRepoPath = pair.first.path)
            }.onSuccess {
                messagesState.success = AppStore.SuccessState(message = "Worktree removed")
            }.onFailure { throwable ->
                messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            }
        }
    }
}

internal fun AppRuntime.onCompleteWorktree(
    worktreePath: String,
    targetBranch: String,
    mergeIntoTarget: Boolean,
    pullTargetFirst: Boolean,
    deleteLocalBranch: Boolean,
    deleteRemoteBranch: Boolean,
    force: Boolean,
) {
    val pair = findWorktreeByPath(path = worktreePath) ?: return
    val options =
        CompleteWorktreeOptions(
            targetBranch = targetBranch,
            mergeIntoTarget = mergeIntoTarget,
            pullTargetFirst = pullTargetFirst,
            deleteLocalBranch = deleteLocalBranch,
            deleteRemoteBranch = deleteRemoteBranch,
            force = force,
        )
    scope.launch {
        withWorktreeLoading(pair.second.path, Res.string.loading_completing) {
            runCatching {
                if (pair.second.isMain) {
                    throw AppError.CannotRemoveMainWorktree()
                }
                if (options.mergeIntoTarget) {
                    graph.gitClient.mergeBranch(
                        atRepoPath = pair.first.path,
                        source = pair.second.branch,
                        intoTarget = options.targetBranch,
                    )
                }
                if (options.pullTargetFirst) {
                    val repoWorktrees = graph.gitClient.listWorktrees(atRepoPath = pair.first.path)
                    val targetWorktree = repoWorktrees.firstOrNull { it.branch == options.targetBranch }
                    if (targetWorktree != null) {
                        graph.gitClient.pull(atWorktreePath = targetWorktree.path)
                    }
                }
                graph.gitClient.removeWorktree(
                    atRepoPath = pair.first.path,
                    worktreePath = pair.second.path,
                    force = options.force,
                )
                graph.preferencesStore.removeWorktreeBaseBranch(forWorktreePath = pair.second.path)
                if (options.deleteLocalBranch && pair.second.branch.isNotBlank() && pair.second.branch != "detached HEAD") {
                    graph.gitClient.deleteBranch(
                        atRepoPath = pair.first.path,
                        branch = pair.second.branch,
                        force = options.force,
                    )
                }
                if (options.deleteRemoteBranch && pair.second.branch.isNotBlank()) {
                    graph.gitClient.deleteRemoteBranch(
                        atRepoPath = pair.first.path,
                        branch = pair.second.branch,
                    )
                }
                if (worktreesState.selectedWorktreePath == pair.second.path) {
                    worktreesState.selectedWorktreePath = null
                    persistSelection()
                }
                loadWorktreesForRepositoryInternal(path = pair.first.path)
                settingsState.branches = graph.gitClient.listBranches(atRepoPath = pair.first.path)
            }.onSuccess {
                messagesState.success = AppStore.SuccessState(message = "Worktree completed")
            }.onFailure { throwable ->
                messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            }
        }
    }
}

internal fun AppRuntime.onLoadHasRemoteBranch(worktreePath: String) {
    val pair = findWorktreeByPath(path = worktreePath) ?: return
    scope.launch {
        runCatching {
            graph.gitClient.hasRemoteBranch(
                atRepoPath = pair.first.path,
                branch = pair.second.branch,
            )
        }.onSuccess { hasRemote ->
            worktreesState.hasRemoteBranchByWorktreePath[pair.second.path] = hasRemote
            publishState()
        }.onFailure { throwable ->
            messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            publishState()
        }
    }
}

internal fun AppRuntime.onPruneWorktrees() {
    val repositoryPath = selectedRepository()?.path ?: return
    scope.launch {
        withGlobalLoading(Res.string.loading_pruning) {
            runCatching {
                graph.gitClient.pruneWorktrees(atRepoPath = repositoryPath)
                loadWorktreesForRepositoryInternal(path = repositoryPath)
            }.onSuccess {
                messagesState.success = AppStore.SuccessState(message = "Worktrees pruned")
            }.onFailure { throwable ->
                messagesState.error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            }
        }
    }
}
