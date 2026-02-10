package app.tich.buildandrun.presentation.app.context.worktrees.impl.usecase

import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.worktrees.usecase.LoadBranchesUseCase
import app.tich.buildandrun.presentation.app.core.AppWiring
import app.tich.buildandrun.presentation.app.core.mapFailureToErrorState
import app.tich.buildandrun.presentation.app.core.publishState
import app.tich.buildandrun.presentation.app.core.selectedRepository
import kotlinx.coroutines.launch

class DefaultLoadSelectedRepositoryBranchesUseCase(
    private val runtime: AppWiring,
) : LoadSelectedRepositoryBranchesUseCase {
    override fun execute() {
        val repositoryPath = runtime.selectedRepository()?.path ?: return
        runtime.scope.launch {
            when (
                val result =
                    runtime.graph.loadBranchesUseCase.execute(
                        input =
                            LoadBranchesUseCase.Input(
                                repositoryPath = repositoryPath,
                            ),
                    )
            ) {
                is UseCaseResult.Success -> {
                    runtime.settingsState.branches = result.value.branches
                }

                is UseCaseResult.Failure -> {
                    runtime.messagesState.error = runtime.mapFailureToErrorState(result.value)
                }
            }
            runtime.publishState()
        }
    }
}
