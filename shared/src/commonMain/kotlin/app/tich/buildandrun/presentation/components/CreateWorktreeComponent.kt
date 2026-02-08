package app.tich.buildandrun.presentation.components

import app.tich.buildandrun.presentation.errors.UiError
import com.arkivanov.decompose.value.Value

interface CreateWorktreeComponent {
    val state: Value<State>

    fun onBranchChanged(value: String)

    fun onWorktreePathChanged(value: String)

    fun onBaseBranchChanged(value: String)

    fun onCreateBranchChanged(value: Boolean)

    fun onCreateWorktree()

    fun onDismissError()

    fun onBack()

    fun onDestroy()

    data class State(
        val repositoryPath: String,
        val branchInput: String = "",
        val worktreePathInput: String = "",
        val baseBranchInput: String = "",
        val createBranch: Boolean = true,
        val isSubmitting: Boolean = false,
        val createdWorktreePath: String? = null,
        val error: UiError? = null,
    )

    sealed interface Output {
        data object BackRequested : Output
    }
}
