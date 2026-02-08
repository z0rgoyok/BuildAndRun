package app.tich.buildandrun.appstore

import app.tich.buildandrun.domain.entities.CompleteWorktreeOptions
import app.tich.buildandrun.domain.errors.AppError
import app.tich.buildandrun.domain.failures.DomainFailureMapper
import kotlinx.coroutines.launch

internal fun AppStoreCore.onPush(worktreePath: String) {
    val pair = findWorktreeByPath(path = worktreePath) ?: return
    scope.launch {
        isLoading = true
        clearMessages()
        publishState()
        runCatching {
            val status = graph.gitClient.getWorktreeStatus(atWorktreePath = pair.second.path)
            graph.gitClient.push(
                atWorktreePath = pair.second.path,
                setUpstream = !status.hasRemote,
            )
            graph.gitClient.getWorktreeStatus(atWorktreePath = pair.second.path)
        }.onSuccess { status ->
            worktreeStatusByPath[pair.second.path] = status
            success = AppStore.SuccessState(message = "Push completed")
        }.onFailure { throwable ->
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        isLoading = false
        publishState()
    }
}

internal fun AppStoreCore.onPull(worktreePath: String) {
    val pair = findWorktreeByPath(path = worktreePath) ?: return
    scope.launch {
        isLoading = true
        clearMessages()
        publishState()
        runCatching {
            graph.gitClient.pull(atWorktreePath = pair.second.path)
            graph.gitClient.getWorktreeStatus(atWorktreePath = pair.second.path)
        }.onSuccess { status ->
            worktreeStatusByPath[pair.second.path] = status
            success = AppStore.SuccessState(message = "Pull completed")
        }.onFailure { throwable ->
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        isLoading = false
        publishState()
    }
}

internal fun AppStoreCore.onCreatePullRequest(
    worktreePath: String,
    title: String,
    body: String,
    baseBranch: String?,
) {
    val pair = findWorktreeByPath(path = worktreePath) ?: return
    scope.launch {
        isLoading = true
        clearMessages()
        publishState()
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
            worktreeStatusByPath[pair.second.path] = result.second
            graph.systemOpening.openURL(url = result.first)
            success = AppStore.SuccessState(message = "Pull request created")
        }.onFailure { throwable ->
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        isLoading = false
        publishState()
    }
}

internal fun AppStoreCore.onOpenPullRequest(worktreePath: String) {
    val pair = findWorktreeByPath(path = worktreePath) ?: return
    scope.launch {
        runCatching {
            val status = graph.gitClient.getWorktreeStatus(atWorktreePath = pair.second.path)
            worktreeStatusByPath[pair.second.path] = status
            status.prStatus?.url
        }.onSuccess { url ->
            if (url != null) {
                graph.systemOpening.openURL(url = url)
            }
        }.onFailure { throwable ->
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            publishState()
        }
    }
}

internal fun AppStoreCore.onLockWorktree(worktreePath: String) {
    val pair = findWorktreeByPath(path = worktreePath) ?: return
    scope.launch {
        isLoading = true
        clearMessages()
        publishState()
        runCatching {
            graph.gitClient.lockWorktree(
                atRepoPath = pair.first.path,
                worktreePath = pair.second.path,
                reason = null,
            )
            loadWorktreesForRepositoryInternal(path = pair.first.path)
        }.onSuccess {
            success = AppStore.SuccessState(message = "Worktree locked")
        }.onFailure { throwable ->
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        isLoading = false
        publishState()
    }
}

internal fun AppStoreCore.onUnlockWorktree(worktreePath: String) {
    val pair = findWorktreeByPath(path = worktreePath) ?: return
    scope.launch {
        isLoading = true
        clearMessages()
        publishState()
        runCatching {
            graph.gitClient.unlockWorktree(
                atRepoPath = pair.first.path,
                worktreePath = pair.second.path,
            )
            loadWorktreesForRepositoryInternal(path = pair.first.path)
        }.onSuccess {
            success = AppStore.SuccessState(message = "Worktree unlocked")
        }.onFailure { throwable ->
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        isLoading = false
        publishState()
    }
}

internal fun AppStoreCore.onRemoveWorktree(
    worktreePath: String,
    force: Boolean,
    deleteBranch: Boolean,
) {
    val pair = findWorktreeByPath(path = worktreePath) ?: return
    scope.launch {
        isLoading = true
        clearMessages()
        publishState()
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
            if (selectedWorktreePath == pair.second.path) {
                selectedWorktreePath = null
                persistSelection()
            }
            loadWorktreesForRepositoryInternal(path = pair.first.path)
            branches = graph.gitClient.listBranches(atRepoPath = pair.first.path)
        }.onSuccess {
            success = AppStore.SuccessState(message = "Worktree removed")
        }.onFailure { throwable ->
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        isLoading = false
        publishState()
    }
}

internal fun AppStoreCore.onCompleteWorktree(
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
        isLoading = true
        clearMessages()
        publishState()
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
            if (selectedWorktreePath == pair.second.path) {
                selectedWorktreePath = null
                persistSelection()
            }
            loadWorktreesForRepositoryInternal(path = pair.first.path)
            branches = graph.gitClient.listBranches(atRepoPath = pair.first.path)
        }.onSuccess {
            success = AppStore.SuccessState(message = "Worktree completed")
        }.onFailure { throwable ->
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        isLoading = false
        publishState()
    }
}

internal fun AppStoreCore.onLoadHasRemoteBranch(worktreePath: String) {
    val pair = findWorktreeByPath(path = worktreePath) ?: return
    scope.launch {
        runCatching {
            graph.gitClient.hasRemoteBranch(
                atRepoPath = pair.first.path,
                branch = pair.second.branch,
            )
        }.onSuccess { hasRemote ->
            hasRemoteBranchByWorktreePath[pair.second.path] = hasRemote
            publishState()
        }.onFailure { throwable ->
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
            publishState()
        }
    }
}

internal fun AppStoreCore.onPruneWorktrees() {
    val repositoryPath = selectedRepository()?.path ?: return
    scope.launch {
        isLoading = true
        clearMessages()
        publishState()
        runCatching {
            graph.gitClient.pruneWorktrees(atRepoPath = repositoryPath)
            loadWorktreesForRepositoryInternal(path = repositoryPath)
        }.onSuccess {
            success = AppStore.SuccessState(message = "Worktrees pruned")
        }.onFailure { throwable ->
            error = mapFailureToErrorState(DomainFailureMapper.fromThrowable(throwable))
        }
        isLoading = false
        publishState()
    }
}
