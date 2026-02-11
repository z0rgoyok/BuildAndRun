package app.tich.buildandrun.application.context.shared.di

import app.tich.buildandrun.application.context.shared.usecase.LoadInstalledEditorsUseCase
import app.tich.buildandrun.application.context.shared.usecase.OpenPathInFinderUseCase
import app.tich.buildandrun.application.context.shared.usecase.OpenPathInTerminalUseCase
import app.tich.buildandrun.application.context.shared.usecase.OpenUrlUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

fun applicationSharedUseCasesModule(): Module =
    module {
        single { LoadInstalledEditorsUseCase(editorOpening = get()) }
        single { OpenUrlUseCase(systemOpening = get()) }
        single { OpenPathInFinderUseCase(systemOpening = get()) }
        single { OpenPathInTerminalUseCase(systemOpening = get()) }
    }
