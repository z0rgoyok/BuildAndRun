package app.tich.buildandrun.presentation.app.di

import app.tich.buildandrun.presentation.app.AppNavigationComponent
import app.tich.buildandrun.presentation.app.AppNavigationFeature
import org.koin.core.module.Module
import org.koin.dsl.module

fun presentationNavigationModule(): Module =
    module {
        single<AppNavigationFeature> { AppNavigationComponent(componentContext = get()) }
    }
