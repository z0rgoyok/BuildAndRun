package app.tich.buildandrun.presentation.app

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.repositories.usecase.*
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.port.FileSystemHandling
import app.tich.buildandrun.application.context.shared.port.SystemOpening
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.application.context.worktrees.usecase.CopyConfiguredFilesUseCase
import app.tich.buildandrun.application.context.worktrees.usecase.CreateWorktreeUseCase
import app.tich.buildandrun.application.context.worktrees.usecase.LoadBranchesUseCase
import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper
import app.tich.buildandrun.presentation.app.context.editors.impl.AppEditorsService
import app.tich.buildandrun.presentation.app.context.gitactions.impl.AppGitActionsService
import app.tich.buildandrun.presentation.app.context.groups.impl.AppGroupsService
import app.tich.buildandrun.presentation.app.context.kanban.impl.AppKanbanService
import app.tich.buildandrun.presentation.app.context.messages.impl.AppMessagesService
import app.tich.buildandrun.presentation.app.context.repositories.impl.AppRepositoriesService
import app.tich.buildandrun.presentation.app.context.settings.impl.AppSettingsService
import app.tich.buildandrun.presentation.app.context.sidebar.impl.AppSidebarService
import app.tich.buildandrun.presentation.app.context.state.*
import app.tich.buildandrun.presentation.app.context.texts.impl.AppTextsService
import app.tich.buildandrun.presentation.app.context.worktrees.impl.AppWorktreesService
import app.tich.buildandrun.presentation.app.context.worktrees.impl.WorktreesOperations
import app.tich.buildandrun.presentation.app.core.*
import com.arkivanov.decompose.ComponentContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun appRootModule(
    graph: AppGraph,
    componentContext: ComponentContext = defaultAppComponentContext(),
    onDestroy: (() -> Unit)? = null,
): Module =
    module {
        single<PreferencesStore> { graph.preferencesStore }
        single<GitClient> { graph.gitClient }
        single<FileSystemHandling> { graph.fileSystem }
        single<EditorOpening> { graph.editorOpening }
        single<SystemOpening> { graph.systemOpening }
        single<AddRepositoryUseCase> { graph.addRepositoryUseCase }
        single<LoadRepositoriesUseCase> { graph.loadRepositoriesUseCase }
        single<CreateWorktreeUseCase> { graph.createWorktreeUseCase }
        single<RemoveRepositoryUseCase> { graph.removeRepositoryUseCase }
        single<SetRepositoryArchivedStateUseCase> { graph.setRepositoryArchivedStateUseCase }
        single<SetRepositoryGroupUseCase> { graph.setRepositoryGroupUseCase }
        single<LoadBranchesUseCase> { graph.loadBranchesUseCase }

        single<ComponentContext> { componentContext }
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
                preferencesStore = get(),
                activityState = get(),
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

        single { CopyConfiguredFilesUseCase(preferencesStore = get(), fileSystemHandling = get()) }

        single<AppNavigationFeature> { AppNavigationComponent(componentContext = get()) }
        single {
            AppWorktreesService(
                executionScope = get(),
                loadingRunner = get(),
                stateRefresher = get(),
                errorMapper = get(),
                repositoriesState = get(),
                worktreesState = get(),
                messagesState = get(),
                gitClient = get(),
                preferencesStore = get(),
                editorOpening = get(),
                createWorktreeUseCase = get(),
                loadBranchesUseCase = get(),
                copyConfiguredFilesUseCase = get(),
            )
        }
        single<AppWorktreesFeature> { get<AppWorktreesService>() }
        single<WorktreesOperations> { get<AppWorktreesService>() }

        single<AppRepositoriesFeature> {
            AppRepositoriesService(
                executionScope = get(),
                loadingRunner = get(),
                stateRefresher = get(),
                errorMapper = get(),
                repositoriesState = get(),
                worktreesState = get(),
                messagesState = get(),
                addRepositoryUseCase = get(),
                removeRepositoryUseCase = get(),
                setRepositoryArchivedStateUseCase = get(),
                worktreesOperations = get(),
            )
        }
        single<AppSettingsFeature> {
            AppSettingsService(
                executionScope = get(),
                stateRefresher = get(),
                errorMapper = get(),
                repositoriesState = get(),
                settingsState = get(),
                worktreesState = get(),
                messagesState = get(),
                preferencesStore = get(),
                loadBranchesUseCase = get(),
            )
        }
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
                gitClient = get(),
                preferencesStore = get(),
                systemOpening = get(),
                worktreesOperations = get(),
            )
        }
        single<AppEditorsFeature> {
            AppEditorsService(
                executionScope = get(),
                stateRefresher = get(),
                errorMapper = get(),
                repositoriesState = get(),
                editorsState = get(),
                messagesState = get(),
                preferencesStore = get(),
                editorOpening = get(),
                systemOpening = get(),
            )
        }
        single<AppKanbanFeature> {
            AppKanbanService(
                stateRefresher = get(),
                kanbanState = get(),
                messagesState = get(),
            )
        }
        single<AppSidebarFeature> {
            AppSidebarService(
                stateRefresher = get(),
                repositoriesState = get(),
                preferencesStore = get(),
            )
        }
        single<AppGroupsFeature> {
            AppGroupsService(
                executionScope = get(),
                stateRefresher = get(),
                errorMapper = get(),
                repositoriesState = get(),
                messagesState = get(),
                preferencesStore = get(),
                setRepositoryGroupUseCase = get(),
            )
        }
        single<AppMessagesFeature> { AppMessagesService(messagesState = get()) }
        single<AppTextsFeature> { AppTextsService() }

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
                preferencesStore = get(),
                loadRepositoriesUseCase = get(),
                editorOpening = get(),
                loadWorktreesForRepositoryInternal = { path -> get<WorktreesOperations>().loadWorktreesForRepositoryInternal(path) },
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
