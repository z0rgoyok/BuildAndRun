package app.tich.buildandrun.presentation.app

import app.tich.buildandrun.application.context.repositories.di.applicationRepositoriesUseCasesModule
import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.repositories.usecase.AppSessionPersistenceUseCase
import app.tich.buildandrun.application.context.repositories.usecase.RestoreAppSessionUseCase
import app.tich.buildandrun.application.context.shared.di.applicationSharedUseCasesModule
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.port.FileSystemHandling
import app.tich.buildandrun.application.context.shared.port.SystemOpening
import app.tich.buildandrun.application.context.worktrees.di.applicationWorktreesUseCasesModule
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.application.context.worktrees.usecase.LoadRepositoryWorktreeSnapshotUseCase
import app.tich.buildandrun.presentation.app.context.editors.di.presentationEditorsModule
import app.tich.buildandrun.presentation.app.context.gitactions.di.presentationGitActionsModule
import app.tich.buildandrun.presentation.app.context.groups.di.presentationGroupsModule
import app.tich.buildandrun.presentation.app.context.kanban.di.presentationKanbanModule
import app.tich.buildandrun.presentation.app.context.messages.di.presentationMessagesModule
import app.tich.buildandrun.presentation.app.context.repositories.di.presentationRepositoriesModule
import app.tich.buildandrun.presentation.app.context.settings.di.presentationSettingsModule
import app.tich.buildandrun.presentation.app.context.sidebar.di.presentationSidebarModule
import app.tich.buildandrun.presentation.app.context.state.*
import app.tich.buildandrun.presentation.app.context.texts.di.presentationTextsModule
import app.tich.buildandrun.presentation.app.context.worktrees.di.presentationWorktreesModule
import app.tich.buildandrun.presentation.app.core.AppBootstrapper
import app.tich.buildandrun.presentation.app.core.AppGraph
import app.tich.buildandrun.presentation.app.di.presentationCommonModule
import app.tich.buildandrun.presentation.app.di.presentationNavigationModule
import com.arkivanov.decompose.ComponentContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun appRootModule(
    graph: AppGraph,
    componentContext: ComponentContext = defaultAppComponentContext(),
    onDestroy: (() -> Unit)? = null,
): Module =
    module {
        includes(
            applicationSharedUseCasesModule(),
            applicationRepositoriesUseCasesModule(),
            applicationWorktreesUseCasesModule(),
            presentationCommonModule(),
            presentationNavigationModule(),
            presentationRepositoriesModule(),
            presentationWorktreesModule(),
            presentationSettingsModule(),
            presentationGitActionsModule(),
            presentationEditorsModule(),
            presentationKanbanModule(),
            presentationSidebarModule(),
            presentationGroupsModule(),
            presentationMessagesModule(),
            presentationTextsModule(),
        )

        single<PreferencesStore> { graph.preferencesStore }
        single<GitClient> { graph.gitClient }
        single<FileSystemHandling> { graph.fileSystem }
        single<EditorOpening> { graph.editorOpening }
        single<SystemOpening> { graph.systemOpening }

        single<ComponentContext> { componentContext }

        single {
            AppBootstrapper(
                executionScope = get(),
                loadingRunner = get(),
                stateRefresher = get(),
                errorMapper = get(),
                repositoriesState = get(),
                worktreesState = get(),
                settingsState = get(),
                editorsState = get(),
                kanbanState = get(),
                messagesState = get(),
                restoreAppSessionUseCase = get<RestoreAppSessionUseCase>(),
                appSessionPersistenceUseCase = get<AppSessionPersistenceUseCase>(),
                loadRepositoryWorktreeSnapshotUseCase = get<LoadRepositoryWorktreeSnapshotUseCase>(),
                reconcileSelectedWorktreePathUseCase = get(),
            )
        }
        single {
            get<AppBootstrapper>().start()
            AppRootComponent(
                executionScope = get(),
                navigation = get(),
                repositories = get(),
                worktrees = get(),
                settings = get(),
                gitActions = get(),
                editors = get(),
                kanban = get(),
                sidebar = get(),
                groups = get(),
                messages = get(),
                texts = get(),
                sidebarLabels = buildSidebarLabels(),
                kanbanLabels = buildKanbanLabels(),
                activityState = get<ActivityContextState>().state,
                repositoriesState = get<RepositoriesContextState>().state,
                worktreesState = get<WorktreesContextState>().state,
                settingsState = get<SettingsContextState>().state,
                editorsState = get<EditorsContextState>().state,
                kanbanState = get<KanbanContextState>().state,
                messagesState = get<MessagesContextState>().state,
                onDestroy = onDestroy,
            )
        }
    }
