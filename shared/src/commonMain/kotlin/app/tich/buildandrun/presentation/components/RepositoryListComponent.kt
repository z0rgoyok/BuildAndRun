package app.tich.buildandrun.presentation.components

import app.tich.buildandrun.presentation.errors.UiError
import com.arkivanov.decompose.value.Value

interface RepositoryListComponent {
    val state: Value<State>

    fun onRepositoryPathChanged(value: String)

    fun onAddRepository()

    fun onCreateWorktree(repositoryPath: String)

    fun onDismissError()

    fun onDestroy()

    data class State(
        val repositoryPathInput: String = "",
        val isLoading: Boolean = false,
        val repositories: List<RepositoryModel> = emptyList(),
        val error: UiError? = null,
    )

    data class RepositoryModel(
        val id: String,
        val name: String,
        val path: String,
    )

    sealed interface Output {
        data class CreateWorktreeRequested(val repositoryPath: String) : Output
    }
}
