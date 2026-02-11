package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.domain.context.kanban.model.KanbanTask
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId

class PersistKanbanTasksUseCase(
    private val preferencesStore: PreferencesStore,
) {
    fun execute(input: Input): UseCaseResult<Output> {
        return runCatchingCancellable {
            preferencesStore.setKanbanTasks(
                tasks = input.tasks,
                forRepositoryId = RepositoryId(value = input.repositoryId),
            )
            UseCaseResult.Success(value = Output)
        }.fold(
            onSuccess = { it },
            onFailure = { throwable ->
                throwable.toUseCaseFailure()
            },
        )
    }

    data class Input(
        val repositoryId: String,
        val tasks: List<KanbanTask>,
    )

    data object Output
}
