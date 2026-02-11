package app.tich.buildandrun.presentation.app.context.repositories.di

import app.tich.buildandrun.presentation.app.AppRepositoriesFeature
import app.tich.buildandrun.presentation.app.context.repositories.impl.AppRepositoriesService
import org.koin.core.module.Module
import org.koin.dsl.module

fun presentationRepositoriesModule(): Module =
    module {
        single<AppRepositoriesFeature> {
            AppRepositoriesService(
                executionScope = get(),
                loadingRunner = get(),
                stateRefresher = get(),
                errorMapper = get(),
                repositoriesState = get(),
                worktreesState = get(),
                kanbanState = get(),
                messagesState = get(),
                addRepositoryUseCase = get(),
                removeRepositoryUseCase = get(),
                setRepositoryArchivedStateUseCase = get(),
                appSessionPersistenceUseCase = get(),
                clearKanbanTasksUseCase = get(),
                loadRepositoryWorktreesUseCase = get(),
            )
        }
    }
