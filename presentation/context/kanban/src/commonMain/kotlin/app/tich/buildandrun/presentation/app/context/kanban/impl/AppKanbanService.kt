package app.tich.buildandrun.presentation.app.context.kanban.impl

import app.tich.buildandrun.application.context.repositories.usecase.*
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.kanban.model.KanbanColumnType
import app.tich.buildandrun.domain.context.kanban.model.KanbanTask
import app.tich.buildandrun.presentation.app.AppKanbanFeature
import app.tich.buildandrun.presentation.app.context.state.KanbanContextState
import app.tich.buildandrun.presentation.app.context.state.MessagesContextState
import app.tich.buildandrun.presentation.app.context.state.repositoryScopeKey
import app.tich.buildandrun.presentation.app.core.AppErrorStateMapper
import app.tich.buildandrun.presentation.app.core.AppStateRefresher
import app.tich.buildandrun.presentation.app.core.currentEpochMillis

class AppKanbanService(
    private val stateRefresher: AppStateRefresher,
    private val errorMapper: AppErrorStateMapper,
    private val kanbanState: KanbanContextState,
    private val messagesState: MessagesContextState,
    private val addKanbanTaskUseCase: AddKanbanTaskUseCase,
    private val moveKanbanTaskUseCase: MoveKanbanTaskUseCase,
    private val deleteKanbanTaskUseCase: DeleteKanbanTaskUseCase,
    private val updateKanbanTaskUseCase: UpdateKanbanTaskUseCase,
    private val persistKanbanTasksUseCase: PersistKanbanTasksUseCase,
) : AppKanbanFeature {
    override fun onAddTask(
        title: String,
        description: String?,
        column: KanbanColumnType,
    ) {
        val scope = currentKanbanScope() ?: return
        when (
            val result =
                addKanbanTaskUseCase.execute(
                    input =
                        AddKanbanTaskUseCase.Input(
                            title = title,
                            description = description,
                            column = column,
                            createdAt = currentEpochMillis(),
                            currentTasks = scope.existingTasks,
                        ),
                )
        ) {
            is UseCaseResult.Success -> {
                persistKanbanTasks(scope = scope, tasks = result.value.tasks)
                messagesState.clear()
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
        stateRefresher.publishAll()
    }

    override fun onMoveTask(
        taskId: String,
        column: KanbanColumnType,
    ) {
        val scope = currentKanbanScope() ?: return
        when (
            val result =
                moveKanbanTaskUseCase.execute(
                    input =
                        MoveKanbanTaskUseCase.Input(
                            taskId = taskId,
                            column = column,
                            currentTasks = scope.existingTasks,
                        ),
                )
        ) {
            is UseCaseResult.Success -> {
                if (result.value.changed) {
                    persistKanbanTasks(scope = scope, tasks = result.value.tasks)
                    messagesState.clear()
                }
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
        stateRefresher.publishAll()
    }

    override fun onDeleteTask(taskId: String) {
        val scope = currentKanbanScope() ?: return
        when (
            val result =
                deleteKanbanTaskUseCase.execute(
                    input =
                        DeleteKanbanTaskUseCase.Input(
                            taskId = taskId,
                            currentTasks = scope.existingTasks,
                        ),
                )
        ) {
            is UseCaseResult.Success -> {
                if (result.value.changed) {
                    persistKanbanTasks(scope = scope, tasks = result.value.tasks)
                    messagesState.clear()
                }
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
        stateRefresher.publishAll()
    }

    override fun onUpdateTask(
        taskId: String,
        title: String,
        description: String?,
    ) {
        val scope = currentKanbanScope() ?: return
        when (
            val result =
                updateKanbanTaskUseCase.execute(
                    input =
                        UpdateKanbanTaskUseCase.Input(
                            taskId = taskId,
                            title = title,
                            description = description,
                            currentTasks = scope.existingTasks,
                        ),
                )
        ) {
            is UseCaseResult.Success -> {
                if (result.value.changed) {
                    persistKanbanTasks(scope = scope, tasks = result.value.tasks)
                    messagesState.clear()
                }
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
        stateRefresher.publishAll()
    }

    private fun currentKanbanScope(): KanbanScope? {
        val repositoryId = stateRefresher.currentRepositoryId() ?: return null
        val scopeKey = repositoryScopeKey(repositoryId = repositoryId)
        return KanbanScope(
            repositoryId = repositoryId,
            scopeKey = scopeKey,
            existingTasks = kanbanState.tasksByScope[scopeKey].orEmpty(),
        )
    }

    private fun persistKanbanTasks(
        scope: KanbanScope,
        tasks: List<KanbanTask>,
    ) {
        kanbanState.tasksByScope[scope.scopeKey] = tasks.toMutableList()
        when (
            val result =
                persistKanbanTasksUseCase.execute(
                    input = PersistKanbanTasksUseCase.Input(repositoryId = scope.repositoryId, tasks = tasks),
                )
        ) {
            is UseCaseResult.Success -> {
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
    }

    private class KanbanScope(
        val repositoryId: String,
        val scopeKey: String,
        val existingTasks: List<KanbanTask>,
    )
}
