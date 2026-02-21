package app.tich.buildandrun.presentation.app.context.editors.di

import app.tich.buildandrun.presentation.app.AppEditorsFeature
import app.tich.buildandrun.presentation.app.context.editors.impl.AppEditorsService
import org.koin.core.module.Module
import org.koin.dsl.module

fun presentationEditorsModule(): Module =
    module {
        single<AppEditorsFeature> {
            AppEditorsService(
                executionScope = get(),
                stateRefresher = get(),
                errorMapper = get(),
                repositoriesState = get(),
                editorsState = get(),
                messagesState = get(),
                setRememberEditorChoiceUseCase = get(),
                setEditorEnabledUseCase = get(),
                setPreferredEditorUseCase = get(),
                openInEditorUseCase = get(),
                openPathInFinderUseCase = get(),
                openPathInTerminalUseCase = get(),
            )
        }
    }
