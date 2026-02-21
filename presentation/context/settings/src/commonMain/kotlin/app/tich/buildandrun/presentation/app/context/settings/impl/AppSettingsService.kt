package app.tich.buildandrun.presentation.app.context.settings.impl

import app.tich.buildandrun.application.context.repositories.usecase.*
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.worktrees.usecase.LoadBranchesUseCase
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
    private val setWorktreeBasePathUseCase: SetWorktreeBasePathUseCase,
    private val loadPreferredBaseBranchUseCase: LoadPreferredBaseBranchUseCase,
    private val setPreferredBaseBranchUseCase: SetPreferredBaseBranchUseCase,
    private val setDefaultCopyPatternsUseCase: SetDefaultCopyPatternsUseCase,
    private val setRepositoryCopyPatternsUseCase: SetRepositoryCopyPatternsUseCase,
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
        when (val result = setWorktreeBasePathUseCase.execute(SetWorktreeBasePathUseCase.Input(path = path))) {
            is UseCaseResult.Success -> {
                settingsState.worktreeBasePath = result.value.path
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
        stateRefresher.publishAll()
    }

    override fun preferredBaseBranch(): String? {
        val repository = repositoriesState.selectedRepository() ?: return null
        return when (
            val result =
                loadPreferredBaseBranchUseCase.execute(
                    input = LoadPreferredBaseBranchUseCase.Input(repositoryId = repository.id.value),
                )
        ) {
            is UseCaseResult.Success -> result.value.branch
            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                null
            }
        }
    }

    override fun onSetPreferredBaseBranch(branch: String) {
        val repository = repositoriesState.selectedRepository() ?: return
        when (
            val result =
                setPreferredBaseBranchUseCase.execute(
                    input =
                        SetPreferredBaseBranchUseCase.Input(
                            repositoryId = repository.id.value,
                            branch = branch,
                        ),
                )
        ) {
            is UseCaseResult.Success -> {
                worktreesState.createWorktreeState =
                    worktreesState.createWorktreeState.copy(baseBranchInput = result.value.branch)
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
        stateRefresher.publishAll()
    }

    override fun onSetDefaultCopyPatterns(patterns: List<String>) {
        when (
            val result =
                setDefaultCopyPatternsUseCase.execute(
                    input = SetDefaultCopyPatternsUseCase.Input(patterns = patterns),
                )
        ) {
            is UseCaseResult.Success -> {
                settingsState.defaultCopyPatterns = result.value.patterns
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
        stateRefresher.publishAll()
    }

    override fun onSetRepositoryCopyPatterns(patterns: List<String>?) {
        val repository = repositoriesState.selectedRepository() ?: return
        when (
            val result =
                setRepositoryCopyPatternsUseCase.execute(
                    input =
                        SetRepositoryCopyPatternsUseCase.Input(
                            repositoryId = repository.id.value,
                            patterns = patterns,
                        ),
                )
        ) {
            is UseCaseResult.Success -> {
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
        stateRefresher.publishAll()
    }
}
