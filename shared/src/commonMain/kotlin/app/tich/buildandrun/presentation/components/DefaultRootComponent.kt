package app.tich.buildandrun.presentation.components

import app.tich.buildandrun.domain.usecases.AddRepositoryUseCase
import app.tich.buildandrun.domain.usecases.CreateWorktreeUseCase
import app.tich.buildandrun.domain.usecases.LoadRepositoriesUseCase
import app.tich.buildandrun.presentation.errors.DomainFailureToUiErrorMapper
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val addRepositoryUseCase: AddRepositoryUseCase,
    private val loadRepositoriesUseCase: LoadRepositoriesUseCase,
    private val createWorktreeUseCase: CreateWorktreeUseCase,
    private val failureToUiErrorMapper: DomainFailureToUiErrorMapper = DomainFailureToUiErrorMapper(),
) : RootComponent,
    ComponentContext by componentContext {
    private var activeChildComponent: Any? = null
    private val mutableChild =
        MutableValue<RootComponent.Child>(
            RootComponent.Child.RepositoryList(
                component = createRepositoryListComponent(),
            ),
        )

    override val child: Value<RootComponent.Child> = mutableChild

    init {
        lifecycle.doOnDestroy {
            destroyActiveChild()
        }
    }

    private fun createRepositoryListComponent(): RepositoryListComponent {
        val component =
            DefaultRepositoryListComponent(
                componentContext = this,
                loadRepositoriesUseCase = loadRepositoriesUseCase,
                addRepositoryUseCase = addRepositoryUseCase,
                failureToUiErrorMapper = failureToUiErrorMapper,
                output = ::onRepositoryListOutput,
            )
        activeChildComponent = component
        return component
    }

    private fun createCreateWorktreeComponent(repositoryPath: String): CreateWorktreeComponent {
        val component =
            DefaultCreateWorktreeComponent(
                componentContext = this,
                repositoryPath = repositoryPath,
                createWorktreeUseCase = createWorktreeUseCase,
                failureToUiErrorMapper = failureToUiErrorMapper,
                output = ::onCreateWorktreeOutput,
            )
        activeChildComponent = component
        return component
    }

    private fun onRepositoryListOutput(output: RepositoryListComponent.Output) {
        when (output) {
            is RepositoryListComponent.Output.CreateWorktreeRequested ->
                switchToCreateWorktree(repositoryPath = output.repositoryPath)
        }
    }

    private fun onCreateWorktreeOutput(output: CreateWorktreeComponent.Output) {
        when (output) {
            CreateWorktreeComponent.Output.BackRequested ->
                switchToRepositoryList()
        }
    }

    private fun switchToRepositoryList() {
        destroyActiveChild()
        mutableChild.value =
            RootComponent.Child.RepositoryList(
                component = createRepositoryListComponent(),
            )
    }

    private fun switchToCreateWorktree(repositoryPath: String) {
        destroyActiveChild()
        mutableChild.value =
            RootComponent.Child.CreateWorktree(
                component = createCreateWorktreeComponent(repositoryPath = repositoryPath),
            )
    }

    private fun destroyActiveChild() {
        when (val component = activeChildComponent) {
            is RepositoryListComponent -> component.onDestroy()
            is CreateWorktreeComponent -> component.onDestroy()
            else -> Unit
        }
        activeChildComponent = null
    }
}
