package app.tich.buildandrun.presentation.app.context.sidebar.di

import app.tich.buildandrun.presentation.app.AppSidebarFeature
import app.tich.buildandrun.presentation.app.context.sidebar.impl.AppSidebarService
import org.koin.core.module.Module
import org.koin.dsl.module

fun presentationSidebarModule(): Module =
    module {
        single<AppSidebarFeature> {
            AppSidebarService(
                stateRefresher = get(),
                errorMapper = get(),
                repositoriesState = get(),
                messagesState = get(),
                setSidebarMembershipStateUseCase = get(),
                toggleSidebarRepositoriesExpansionUseCase = get(),
                syncSidebarSelectionExpansionUseCase = get(),
            )
        }
    }
