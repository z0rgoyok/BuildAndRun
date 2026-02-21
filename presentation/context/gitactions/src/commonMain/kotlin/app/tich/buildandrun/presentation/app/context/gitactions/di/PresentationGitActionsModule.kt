package app.tich.buildandrun.presentation.app.context.gitactions.di

import app.tich.buildandrun.presentation.app.AppGitActionsFeature
import app.tich.buildandrun.presentation.app.context.gitactions.impl.AppGitActionsService
import org.koin.core.module.Module
import org.koin.dsl.module

fun presentationGitActionsModule(): Module =
    module {
        single<AppGitActionsFeature> {
            AppGitActionsService(
                executionScope = get(),
                loadingRunner = get(),
                stateRefresher = get(),
                errorMapper = get(),
                repositoriesState = get(),
                worktreesState = get(),
                settingsState = get(),
                messagesState = get(),
                openUrlUseCase = get(),
                appSessionPersistenceUseCase = get(),
                pushWorktreeUseCase = get(),
                pullWorktreeUseCase = get(),
                createPullRequestUseCase = get(),
                loadPullRequestUrlUseCase = get(),
                lockWorktreeUseCase = get(),
                unlockWorktreeUseCase = get(),
                removeWorktreeUseCase = get(),
                completeWorktreeUseCase = get(),
                loadHasRemoteBranchUseCase = get(),
                pruneWorktreesUseCase = get(),
            )
        }
    }
