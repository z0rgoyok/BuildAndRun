package app.tich.buildandrun.presentation.components

import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.usecases.AddRepositoryUseCase
import app.tich.buildandrun.domain.usecases.LoadRepositoriesUseCase
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

class DefaultRepositoryListComponent(
    componentContext: ComponentContext,
    private val loadRepositoriesUseCase: LoadRepositoriesUseCase,
    private val addRepositoryUseCase: AddRepositoryUseCase,
    private val failureToUiErrorMapper: DomainFailureToUiErrorMapper,
    private val output: (RepositoryListComponent.Output) -> Unit,
) : RepositoryListComponent,
    ComponentContext by componentContext {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutableState = MutableValue(RepositoryListComponent.State())

    override val state: Value<RepositoryListComponent.State> = mutableState

    init {
        lifecycle.doOnDestroy { onDestroy() }
        refreshRepositories()
    }

    override fun onRepositoryPathChanged(value: String) {
        updateState {
            it.copy(repositoryPathInput = value, error = null)
        }
    }

    override fun onAddRepository() {
        val currentState = mutableState.value
        if (currentState.isLoading) {
            return
        }

        scope.launch {
            updateState {
                it.copy(isLoading = true, error = null)
            }

            when (val result = addRepositoryUseCase.execute(input = AddRepositoryUseCase.Input(path = currentState.repositoryPathInput))) {
                is UseCaseResult.Success ->
                    updateState {
                        it.copy(
                            repositoryPathInput = "",
                            isLoading = false,
                            repositories = result.value.repositories.map(::toRepositoryModel),
                            error = null,
                        )
                    }

                is UseCaseResult.Failure ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            error = failureToUiErrorMapper.map(result.value),
                        )
                    }
            }
        }
    }

    override fun onCreateWorktree(repositoryPath: String) {
        val normalizedRepositoryPath = repositoryPath.trim()
        if (normalizedRepositoryPath.isBlank()) {
            return
        }
        output(RepositoryListComponent.Output.CreateWorktreeRequested(repositoryPath = normalizedRepositoryPath))
    }

    override fun onDismissError() {
        updateState {
            it.copy(error = null)
        }
    }

    override fun onDestroy() {
        scope.cancel()
    }

    private fun refreshRepositories() {
        scope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            when (val result = loadRepositoriesUseCase.execute()) {
                is UseCaseResult.Success ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            repositories = result.value.map(::toRepositoryModel),
                            error = null,
                        )
                    }

                is UseCaseResult.Failure ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            error = failureToUiErrorMapper.map(result.value),
                        )
                    }
            }
        }
    }

    private fun toRepositoryModel(repository: Repository): RepositoryListComponent.RepositoryModel =
        RepositoryListComponent.RepositoryModel(
            id = repository.id.value,
            name = repository.name,
            path = repository.path,
        )

    private fun updateState(transform: (RepositoryListComponent.State) -> RepositoryListComponent.State) {
        mutableState.value = transform(mutableState.value)
    }
}
