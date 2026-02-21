package app.tich.buildandrun.application.context.worktrees.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.path.normalizePath
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.domain.context.worktrees.model.Worktree
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

internal fun normalizeAndValidateWorktreeMutationInput(
    repositoryPath: String,
    worktreePath: String,
    isMainWorktree: Boolean,
): UseCaseResult<NormalizedWorktreeMutationInput> {
    val normalizedRepositoryPath = normalizePath(repositoryPath)
    val normalizedWorktreePath = normalizePath(worktreePath)
    val validationFailure =
        validateWorktreeMutationInput(
            repositoryPath = normalizedRepositoryPath,
            worktreePath = normalizedWorktreePath,
            isMainWorktree = isMainWorktree,
        )
    if (validationFailure != null) {
        return UseCaseResult.Failure(value = validationFailure)
    }
    return UseCaseResult.Success(
        value =
            NormalizedWorktreeMutationInput(
                repositoryPath = normalizedRepositoryPath,
                worktreePath = normalizedWorktreePath,
            ),
    )
}

internal inline fun <T> withValidatedWorktreeMutationInput(
    repositoryPath: String,
    worktreePath: String,
    isMainWorktree: Boolean,
    onValid: (NormalizedWorktreeMutationInput) -> UseCaseResult<T>,
): UseCaseResult<T> {
    return when (
        val validated =
            normalizeAndValidateWorktreeMutationInput(
                repositoryPath = repositoryPath,
                worktreePath = worktreePath,
                isMainWorktree = isMainWorktree,
            )
    ) {
        is UseCaseResult.Success -> onValid(validated.value)
        is UseCaseResult.Failure -> validated
    }
}

internal fun validateWorktreeMutationInput(
    repositoryPath: String,
    worktreePath: String,
    isMainWorktree: Boolean,
): DomainFailure? {
    if (repositoryPath.isBlank()) {
        return DomainFailure.Validation(
            code = DomainFailureCode.APP_VALIDATION_REPOSITORY_PATH_BLANK,
            args = emptyList(),
        )
    }
    if (worktreePath.isBlank()) {
        return DomainFailure.Validation(
            code = DomainFailureCode.APP_VALIDATION_WORKTREE_PATH_BLANK,
            args = emptyList(),
        )
    }
    if (isMainWorktree) {
        return DomainFailure.Validation(
            code = DomainFailureCode.APP_CANNOT_REMOVE_MAIN_WORKTREE,
            args = emptyList(),
        )
    }
    return null
}

internal suspend fun removeWorktreeAndLoadSnapshot(
    gitClient: GitClient,
    preferencesStore: PreferencesStore,
    repositoryPath: String,
    worktreePath: String,
    branch: String,
    isDetachedHead: Boolean,
    force: Boolean,
    deleteLocalBranch: Boolean,
    deleteRemoteBranch: Boolean,
): WorktreeMutationSnapshot {
    gitClient.removeWorktree(
        atRepoPath = repositoryPath,
        worktreePath = worktreePath,
        force = force,
    )
    preferencesStore.removeWorktreeBaseBranch(forWorktreePath = worktreePath)
    if (deleteLocalBranch && branch.isNotBlank() && !isDetachedHead) {
        gitClient.deleteBranch(
            atRepoPath = repositoryPath,
            branch = branch,
            force = force,
        )
    }
    if (deleteRemoteBranch && branch.isNotBlank() && !isDetachedHead) {
        gitClient.deleteRemoteBranch(
            atRepoPath = repositoryPath,
            branch = branch,
        )
    }
    val worktrees =
        gitClient.listWorktrees(atRepoPath = repositoryPath).map { worktree ->
            val baseBranch = preferencesStore.worktreeBaseBranch(forWorktreePath = worktree.path)
            worktree.withBaseBranch(baseBranch = baseBranch)
        }
    val branches = gitClient.listBranches(atRepoPath = repositoryPath)
    return WorktreeMutationSnapshot(worktrees = worktrees, branches = branches)
}

internal data class WorktreeMutationSnapshot(
    val worktrees: List<Worktree>,
    val branches: List<String>,
)

internal data class NormalizedWorktreeMutationInput(
    val repositoryPath: String,
    val worktreePath: String,
)
