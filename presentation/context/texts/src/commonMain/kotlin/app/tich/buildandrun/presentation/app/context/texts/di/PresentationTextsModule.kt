package app.tich.buildandrun.presentation.app.context.texts.di

import app.tich.buildandrun.presentation.app.AppTextsFeature
import app.tich.buildandrun.presentation.app.context.texts.impl.AppTextsService
import org.koin.core.module.Module
import org.koin.dsl.module

fun presentationTextsModule(): Module =
    module {
        single<AppTextsFeature> { AppTextsService() }
    }
