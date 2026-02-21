package app.tich.buildandrun.presentation.app.context.settings.di

import app.tich.buildandrun.presentation.app.AppSettingsFeature
import app.tich.buildandrun.presentation.app.context.settings.impl.AppSettingsService
import org.koin.core.module.Module
import org.koin.dsl.module

fun presentationSettingsModule(): Module =
    module {
        single<AppSettingsFeature> {
            AppSettingsService(
                executionScope = get(),
                stateRefresher = get(),
                errorMapper = get(),
                repositoriesState = get(),
                settingsState = get(),
                worktreesState = get(),
                messagesState = get(),
                setWorktreeBasePathUseCase = get(),
                loadPreferredBaseBranchUseCase = get(),
                setPreferredBaseBranchUseCase = get(),
                setDefaultCopyPatternsUseCase = get(),
                setRepositoryCopyPatternsUseCase = get(),
                loadBranchesUseCase = get(),
            )
        }
    }
