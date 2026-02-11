package app.tich.buildandrun.presentation.app.context.kanban.di

import app.tich.buildandrun.presentation.app.AppKanbanFeature
import app.tich.buildandrun.presentation.app.context.kanban.impl.AppKanbanService
import org.koin.core.module.Module
import org.koin.dsl.module

fun presentationKanbanModule(): Module =
    module {
        single<AppKanbanFeature> {
            AppKanbanService(
                stateRefresher = get(),
                errorMapper = get(),
                kanbanState = get(),
                messagesState = get(),
                addKanbanTaskUseCase = get(),
                moveKanbanTaskUseCase = get(),
                deleteKanbanTaskUseCase = get(),
                updateKanbanTaskUseCase = get(),
                persistKanbanTasksUseCase = get(),
            )
        }
    }
