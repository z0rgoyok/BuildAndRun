package app.tich.buildandrun.presentation.components

import app.tich.buildandrun.domain.usecases.CreateWorktreeUseCase
import app.tich.buildandrun.domain.usecases.UseCaseResult
import app.tich.buildandrun.presentation.errors.DomainFailureToUiErrorMapper
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultCreateWorktreeComponent(
    componentContext: ComponentContext,
    repositoryPath: String,
    private val createWorktreeUseCase: CreateWorktreeUseCase,
    private val failureToUiErrorMapper: DomainFailureToUiErrorMapper,
    private val output: (CreateWorktreeComponent.Output) -> Unit,
) : CreateWorktreeComponent,
    ComponentContext by componentContext {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutableState =
        MutableValue(
            CreateWorktreeComponent.State(
                repositoryPath = repositoryPath.trim(),
            ),
        )

    override val state: Value<CreateWorktreeComponent.State> = mutableState

    init {
        lifecycle.doOnDestroy { onDestroy() }
    }

    override fun onBranchChanged(value: String) {
        updateState {
            it.copy(branchInput = value, error = null, createdWorktreePath = null)
        }
    }

    override fun onWorktreePathChanged(value: String) {
        updateState {
            it.copy(worktreePathInput = value, error = null, createdWorktreePath = null)
        }
    }

    override fun onBaseBranchChanged(value: String) {
        updateState {
            it.copy(baseBranchInput = value, error = null)
        }
    }

    override fun onCreateBranchChanged(value: Boolean) {
        updateState {
            it.copy(createBranch = value, error = null)
        }
    }

    override fun onCreateWorktree() {
        val currentState = mutableState.value
        if (currentState.isSubmitting) {
            return
        }

        scope.launch {
            updateState {
                it.copy(
                    isSubmitting = true,
                    error = null,
                    createdWorktreePath = null,
                )
            }

            when (
                val result =
                    createWorktreeUseCase.execute(
                        input =
                            CreateWorktreeUseCase.Input(
                                repositoryPath = currentState.repositoryPath,
                                branch = currentState.branchInput,
                                worktreePath = currentState.worktreePathInput,
                                createBranch = currentState.createBranch,
                                baseBranch = currentState.baseBranchInput,
                            ),
                    )
            ) {
                is UseCaseResult.Success ->
                    updateState {
                        it.copy(
                            isSubmitting = false,
                            createdWorktreePath = result.value.createdWorktree.path,
                            error = null,
                        )
                    }

                is UseCaseResult.Failure ->
                    updateState {
                        it.copy(
                            isSubmitting = false,
                            error = failureToUiErrorMapper.map(result.value),
                        )
                    }
            }
        }
    }

    override fun onDismissError() {
        updateState {
            it.copy(error = null)
        }
    }

    override fun onBack() {
        output(CreateWorktreeComponent.Output.BackRequested)
    }

    override fun onDestroy() {
        scope.cancel()
    }

    private fun updateState(transform: (CreateWorktreeComponent.State) -> CreateWorktreeComponent.State) {
        mutableState.value = transform(mutableState.value)
    }
}
