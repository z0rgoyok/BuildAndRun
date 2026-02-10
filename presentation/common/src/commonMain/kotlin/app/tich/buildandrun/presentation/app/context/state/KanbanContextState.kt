package app.tich.buildandrun.presentation.app.context.state

import app.tich.buildandrun.domain.context.kanban.model.KanbanTask
import app.tich.buildandrun.presentation.app.KanbanState
import app.tich.buildandrun.presentation.app.KanbanTaskItem
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

class KanbanContextState {
    private val mutableState = MutableValue(KanbanState())

    val tasksByScope = mutableMapOf<String, MutableList<KanbanTask>>()

    val state: Value<KanbanState> = mutableState

    fun publish(selectedRepositoryId: String?) {
        mutableState.value = KanbanState(kanbanTasks = currentKanbanTasks(selectedRepositoryId = selectedRepositoryId))
    }

    fun currentKanbanTasks(selectedRepositoryId: String?): List<KanbanTaskItem> {
        val repositoryId = selectedRepositoryId ?: return emptyList()
        val scopeKey = repositoryScopeKey(repositoryId = repositoryId)
        val tasks = tasksByScope[scopeKey].orEmpty()
        return tasks
            .sortedWith(compareBy<KanbanTask> { it.columnId.ordinal }.thenBy { it.order })
            .map {
                KanbanTaskItem(
                    id = it.id.value,
                    title = it.title,
                    description = it.description,
                    columnId = it.columnId,
                    order = it.order,
                )
            }
    }
}

fun repositoryScopeKey(repositoryId: String): String = "repo:$repositoryId"
