package app.tich.buildandrun.presentation.app.di

import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper
import app.tich.buildandrun.presentation.app.context.state.*
import app.tich.buildandrun.presentation.app.core.AppErrorStateMapper
import app.tich.buildandrun.presentation.app.core.AppExecutionScope
import app.tich.buildandrun.presentation.app.core.AppLoadingRunner
import app.tich.buildandrun.presentation.app.core.AppStateRefresher
import org.koin.core.module.Module
import org.koin.dsl.module

fun presentationCommonModule(): Module =
    module {
        single { AppErrorStateMapper() }
        single { ActivityContextState() }
        single { RepositoriesContextState() }
        single { WorktreesContextState() }
        single { SettingsContextState() }
        single { EditorsContextState() }
        single { KanbanContextState() }
        single { MessagesContextState() }
        single {
            AppStateRefresher(
                activityState = get(),
                errorMapper = get(),
                loadPresentationPreferencesUseCase = get(),
                loadInstalledEditorsUseCase = get(),
                repositoriesState = get(),
                worktreesState = get(),
                settingsState = get(),
                editorsState = get(),
                kanbanState = get(),
                messagesState = get(),
            )
        }
        single {
            AppExecutionScope(
                onUnhandledError = { throwable ->
                    get<MessagesContextState>().error =
                        get<AppErrorStateMapper>().mapFailureToErrorState(
                            DomainFailureMapper.fromThrowable(throwable),
                        )
                    get<AppStateRefresher>().publishAll()
                },
            )
        }
        single { AppLoadingRunner(stateRefresher = get(), messagesState = get()) }
    }
