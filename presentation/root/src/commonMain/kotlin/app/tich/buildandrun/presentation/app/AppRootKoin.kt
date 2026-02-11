package app.tich.buildandrun.presentation.app

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.repositories.usecase.*
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.port.FileSystemHandling
import app.tich.buildandrun.application.context.shared.port.SystemOpening
import app.tich.buildandrun.application.context.shared.usecase.LoadInstalledEditorsUseCase
import app.tich.buildandrun.application.context.shared.usecase.OpenPathInFinderUseCase
import app.tich.buildandrun.application.context.shared.usecase.OpenPathInTerminalUseCase
import app.tich.buildandrun.application.context.shared.usecase.OpenUrlUseCase
import app.tich.buildandrun.application.context.worktrees.port.GitClient
import app.tich.buildandrun.application.context.worktrees.usecase.*
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

        single { RestoreAppSessionUseCase(preferencesStore = get(), loadRepositoriesUseCase = get()) }
        single { AppSessionPersistenceUseCase(preferencesStore = get()) }
        single { PersistKanbanTasksUseCase(preferencesStore = get()) }
        single { ClearKanbanTasksUseCase(preferencesStore = get()) }

        single { ReorderRepositoryGroupsUseCase(preferencesStore = get()) }
        single { CreateRepositoryGroupUseCase(preferencesStore = get()) }
        single { RenameRepositoryGroupUseCase(preferencesStore = get()) }
        single { DeleteRepositoryGroupUseCase(preferencesStore = get()) }

        single { SetWorktreeBasePathUseCase(preferencesStore = get()) }
        single { LoadPreferredBaseBranchUseCase(preferencesStore = get()) }
        single { SetPreferredBaseBranchUseCase(preferencesStore = get()) }
        single { SetDefaultCopyPatternsUseCase(preferencesStore = get()) }
        single { SetRepositoryCopyPatternsUseCase(preferencesStore = get()) }

        single { SetRememberEditorChoiceUseCase(preferencesStore = get()) }
        single { SetEditorEnabledUseCase(preferencesStore = get()) }
        single { SetPreferredEditorUseCase(preferencesStore = get()) }
        single { OpenInEditorUseCase(preferencesStore = get(), editorOpening = get()) }
        single { LoadPresentationPreferencesUseCase(preferencesStore = get()) }

        single { SetSidebarMembershipStateUseCase(preferencesStore = get()) }
        single { ToggleSidebarRepositoriesExpansionUseCase(preferencesStore = get()) }
        single { SyncSidebarSelectionExpansionUseCase(preferencesStore = get()) }

        single { AddKanbanTaskUseCase() }
        single { MoveKanbanTaskUseCase() }
        single { DeleteKanbanTaskUseCase() }
        single { UpdateKanbanTaskUseCase() }

        single { CopyConfiguredFilesUseCase(preferencesStore = get(), fileSystemHandling = get()) }
        single { LoadRepositoryWorktreesUseCase(gitClient = get(), preferencesStore = get()) }
        single { LoadWorktreeStatusUseCase(gitClient = get()) }
        single {
            CreateWorktreeFlowUseCase(
                createWorktreeUseCase = get(),
                copyConfiguredFilesUseCase = get(),
                loadRepositoryWorktreesUseCase = get(),
                loadBranchesUseCase = get(),
                preferencesStore = get(),
            )
        }
        single { PushWorktreeUseCase(gitClient = get()) }
        single { PullWorktreeUseCase(gitClient = get()) }
        single { CreatePullRequestUseCase(gitClient = get()) }
        single { LoadPullRequestUrlUseCase(gitClient = get()) }
        single { LockWorktreeUseCase(gitClient = get(), preferencesStore = get()) }
        single { UnlockWorktreeUseCase(gitClient = get(), preferencesStore = get()) }
        single { RemoveWorktreeUseCase(gitClient = get(), preferencesStore = get()) }
        single { CompleteWorktreeUseCase(gitClient = get(), preferencesStore = get()) }
        single { LoadHasRemoteBranchUseCase(gitClient = get()) }
        single { PruneWorktreesUseCase(gitClient = get(), preferencesStore = get()) }
        single { LoadInstalledEditorsUseCase(editorOpening = get()) }
        single { OpenUrlUseCase(systemOpening = get()) }
        single { OpenPathInFinderUseCase(systemOpening = get()) }
        single { OpenPathInTerminalUseCase(systemOpening = get()) }

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
                createWorktreeFlowUseCase = get(),
                loadBranchesUseCase = get(),
                loadRepositoryWorktreesUseCase = get(),
                loadWorktreeStatusUseCase = get(),
                appSessionPersistenceUseCase = get(),
            )
        }
        single<AppWorktreesFeature> { get<AppWorktreesService>() }

        single<AppRepositoriesFeature> {
            AppRepositoriesService(
                executionScope = get(),
                loadingRunner = get(),
                stateRefresher = get(),
                errorMapper = get(),
                repositoriesState = get(),
                worktreesState = get(),
                kanbanState = get(),
                messagesState = get(),
                addRepositoryUseCase = get(),
                removeRepositoryUseCase = get(),
                setRepositoryArchivedStateUseCase = get(),
                appSessionPersistenceUseCase = get(),
                clearKanbanTasksUseCase = get(),
                loadRepositoryWorktreesUseCase = get(),
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
                setWorktreeBasePathUseCase = get(),
                loadPreferredBaseBranchUseCase = get(),
                setPreferredBaseBranchUseCase = get(),
                setDefaultCopyPatternsUseCase = get(),
                setRepositoryCopyPatternsUseCase = get(),
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
        single<AppKanbanFeature> {
            AppKanbanService(
                stateRefresher = get(),
                errorMapper = get(),
                kanbanState = get(),
                messagesState = get(),
                addKanbanTaskUseCase = get(),
                moveKanbanTaskUseCase = get(),
                deleteKanbanTaskUseCase = get(),
                updateKanbanTaskUseCase = get(),
                persistKanbanTasksUseCase = get(),
            )
        }
        single<AppSidebarFeature> {
            AppSidebarService(
                stateRefresher = get(),
                repositoriesState = get(),
                setSidebarMembershipStateUseCase = get(),
                toggleSidebarRepositoriesExpansionUseCase = get(),
                syncSidebarSelectionExpansionUseCase = get(),
            )
        }
        single<AppGroupsFeature> {
            AppGroupsService(
                executionScope = get(),
                stateRefresher = get(),
                errorMapper = get(),
                repositoriesState = get(),
                messagesState = get(),
                reorderRepositoryGroupsUseCase = get(),
                createRepositoryGroupUseCase = get(),
                renameRepositoryGroupUseCase = get(),
                deleteRepositoryGroupUseCase = get(),
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
                restoreAppSessionUseCase = get(),
                appSessionPersistenceUseCase = get(),
                loadRepositoryWorktreesUseCase = get(),
                loadWorktreeStatusUseCase = get(),
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
