package app.tich.buildandrun.presentation.app.context.worktrees.di

import app.tich.buildandrun.presentation.app.AppWorktreesFeature
import app.tich.buildandrun.presentation.app.context.worktrees.impl.AppWorktreesService
import org.koin.core.module.Module
import org.koin.dsl.module

fun presentationWorktreesModule(): Module =
    module {
        single {
            AppWorktreesService(
                executionScope = get(),
                loadingRunner = get(),
                stateRefresher = get(),
                errorMapper = get(),
                repositoriesState = get(),
                worktreesState = get(),
                messagesState = get(),
                createWorktreeFlowUseCase = get(),
                loadBranchesUseCase = get(),
                loadRepositoryWorktreesUseCase = get(),
                loadWorktreeStatusUseCase = get(),
                reconcileSelectedWorktreePathUseCase = get(),
                appSessionPersistenceUseCase = get(),
            )
        }
        single<AppWorktreesFeature> { get<AppWorktreesService>() }
    }
