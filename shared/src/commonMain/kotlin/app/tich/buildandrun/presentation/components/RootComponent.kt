package app.tich.buildandrun.presentation.components

import com.arkivanov.decompose.value.Value

interface RootComponent {
    val child: Value<Child>

    sealed class Child {
        data class RepositoryList(
            val component: RepositoryListComponent,
        ) : Child()

        data class CreateWorktree(
            val component: CreateWorktreeComponent,
        ) : Child()
    }
}
