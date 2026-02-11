package app.tich.buildandrun.presentation.app.context.groups.di

import app.tich.buildandrun.presentation.app.AppGroupsFeature
import app.tich.buildandrun.presentation.app.context.groups.impl.AppGroupsService
import org.koin.core.module.Module
import org.koin.dsl.module

fun presentationGroupsModule(): Module =
    module {
        single<AppGroupsFeature> {
            AppGroupsService(
                executionScope = get(),
                stateRefresher = get(),
                errorMapper = get(),
                repositoriesState = get(),
                messagesState = get(),
                reorderRepositoryGroupsUseCase = get(),
                createRepositoryGroupUseCase = get(),
                renameRepositoryGroupUseCase = get(),
                deleteRepositoryGroupUseCase = get(),
                setRepositoryGroupUseCase = get(),
                createGroupAndAssignRepositoryUseCase = get(),
            )
        }
    }
