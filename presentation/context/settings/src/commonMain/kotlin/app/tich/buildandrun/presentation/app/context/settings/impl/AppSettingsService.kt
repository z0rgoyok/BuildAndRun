package app.tich.buildandrun.presentation.app.context.settings.impl

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.worktrees.usecase.LoadBranchesUseCase
import app.tich.buildandrun.domain.context.copy.model.CopyPattern
import app.tich.buildandrun.presentation.app.AppSettingsFeature
import app.tich.buildandrun.presentation.app.context.state.MessagesContextState
import app.tich.buildandrun.presentation.app.context.state.RepositoriesContextState
import app.tich.buildandrun.presentation.app.context.state.SettingsContextState
import app.tich.buildandrun.presentation.app.context.state.WorktreesContextState
import app.tich.buildandrun.presentation.app.core.AppErrorStateMapper
import app.tich.buildandrun.presentation.app.core.AppExecutionScope
import app.tich.buildandrun.presentation.app.core.AppStateRefresher
import kotlinx.coroutines.launch

class AppSettingsService(
    private val executionScope: AppExecutionScope,
    private val stateRefresher: AppStateRefresher,
    private val errorMapper: AppErrorStateMapper,
    private val repositoriesState: RepositoriesContextState,
    private val settingsState: SettingsContextState,
    private val worktreesState: WorktreesContextState,
    private val messagesState: MessagesContextState,
    private val preferencesStore: PreferencesStore,
    private val loadBranchesUseCase: LoadBranchesUseCase,
) : AppSettingsFeature {
    override fun onLoadBranches() {
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
                    settingsState.branches = result.value.branches
                }

                is UseCaseResult.Failure -> {
                    messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                }
            }
            stateRefresher.publishAll()
        }
    }

    override fun branchExists(branch: String): Boolean {
        repositoriesState.selectedRepository()?.path ?: return false
        val normalizedBranch = branch.trim()
        if (normalizedBranch.isBlank()) {
            return false
        }
        return settingsState.branches.any { it == normalizedBranch }
    }

    override fun onSetWorktreeBasePath(path: String) {
        val normalizedPath = path.trim()
        settingsState.worktreeBasePath = normalizedPath
        preferencesStore.worktreeBasePath = normalizedPath
        stateRefresher.publishAll()
    }

    override fun preferredBaseBranch(): String? {
        val repository = repositoriesState.selectedRepository() ?: return null
        return preferencesStore.preferredBaseBranch(forRepositoryId = repository.id)
    }

    override fun onSetPreferredBaseBranch(branch: String) {
        val repository = repositoriesState.selectedRepository() ?: return
        val normalizedBranch = branch.trim()
        if (normalizedBranch.isBlank()) {
            return
        }
        preferencesStore.setPreferredBaseBranch(
            branch = normalizedBranch,
            forRepositoryId = repository.id,
        )
        worktreesState.createWorktreeState = worktreesState.createWorktreeState.copy(baseBranchInput = normalizedBranch)
        stateRefresher.publishAll()
    }

    override fun onSetDefaultCopyPatterns(patterns: List<String>) {
        val normalizedPatterns =
            patterns
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct()
                .map { pattern -> CopyPattern(pattern = pattern) }
        settingsState.defaultCopyPatterns = normalizedPatterns
        preferencesStore.defaultCopyPatterns = normalizedPatterns
        stateRefresher.publishAll()
    }

    override fun onSetRepositoryCopyPatterns(patterns: List<String>?) {
        val repository = repositoriesState.selectedRepository() ?: return
        if (patterns == null) {
            preferencesStore.removeCopyPatterns(forRepositoryId = repository.id)
        } else {
            val normalizedPatterns =
                patterns
                    .map(String::trim)
                    .filter(String::isNotBlank)
                    .distinct()
                    .map { pattern -> CopyPattern(pattern = pattern) }
            preferencesStore.setCopyPatterns(
                patterns = normalizedPatterns,
                forRepositoryId = repository.id,
            )
        }
        stateRefresher.publishAll()
    }
}
