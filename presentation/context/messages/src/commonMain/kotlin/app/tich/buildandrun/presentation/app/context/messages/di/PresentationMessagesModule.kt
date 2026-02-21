package app.tich.buildandrun.presentation.app.context.messages.di

import app.tich.buildandrun.presentation.app.AppMessagesFeature
import app.tich.buildandrun.presentation.app.context.messages.impl.AppMessagesService
import org.koin.core.module.Module
import org.koin.dsl.module

fun presentationMessagesModule(): Module =
    module {
        single<AppMessagesFeature> { AppMessagesService(messagesState = get()) }
    }
