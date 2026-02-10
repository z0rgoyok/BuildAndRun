package app.tich.buildandrun.presentation.app

import app.tich.buildandrun.presentation.app.context.editors.impl.AppEditorsService
import app.tich.buildandrun.presentation.app.context.gitactions.impl.AppGitActionsService
import app.tich.buildandrun.presentation.app.context.groups.impl.AppGroupsService
import app.tich.buildandrun.presentation.app.context.kanban.impl.AppKanbanService
import app.tich.buildandrun.presentation.app.context.messages.impl.AppMessagesService
import app.tich.buildandrun.presentation.app.context.repositories.impl.AppRepositoriesService
import app.tich.buildandrun.presentation.app.context.settings.impl.AppSettingsService
import app.tich.buildandrun.presentation.app.context.sidebar.impl.AppSidebarService
import app.tich.buildandrun.presentation.app.context.texts.impl.AppTextsService
import app.tich.buildandrun.presentation.app.context.worktrees.impl.AppWorktreesService
import app.tich.buildandrun.presentation.app.context.worktrees.impl.usecase.CopyConfiguredFilesUseCase
import app.tich.buildandrun.presentation.app.context.worktrees.impl.usecase.DefaultCopyConfiguredFilesUseCase
import app.tich.buildandrun.presentation.app.context.worktrees.impl.usecase.DefaultLoadSelectedRepositoryBranchesUseCase
import app.tich.buildandrun.presentation.app.context.worktrees.impl.usecase.LoadSelectedRepositoryBranchesUseCase
import app.tich.buildandrun.presentation.app.core.AppGraph
import app.tich.buildandrun.presentation.app.core.AppWiring
import com.arkivanov.decompose.ComponentContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun appStoreModule(
    graph: AppGraph,
    componentContext: ComponentContext = defaultAppComponentContext(),
    onDestroy: (() -> Unit)? = null,
): Module =
    module {
        single<AppGraph> { graph }
        single<ComponentContext> { componentContext }
        single { AppWiring(graph = get()) }
        single<LoadSelectedRepositoryBranchesUseCase> { DefaultLoadSelectedRepositoryBranchesUseCase(runtime = get()) }
        single<CopyConfiguredFilesUseCase> { DefaultCopyConfiguredFilesUseCase(runtime = get()) }
        single<AppNavigationFeature> { AppNavigationComponent(componentContext = get()) }
        single<AppRepositoriesFeature> { AppRepositoriesService(runtime = get()) }
        single<AppWorktreesFeature> {
            AppWorktreesService(
                runtime = get(),
                loadSelectedRepositoryBranchesUseCase = get(),
                copyConfiguredFilesUseCase = get(),
            )
        }
        single<AppSettingsFeature> { AppSettingsService(runtime = get()) }
        single<AppGitActionsFeature> { AppGitActionsService(runtime = get()) }
        single<AppEditorsFeature> { AppEditorsService(runtime = get()) }
        single<AppKanbanFeature> { AppKanbanService(runtime = get()) }
        single<AppSidebarFeature> { AppSidebarService(runtime = get()) }
        single<AppGroupsFeature> { AppGroupsService(runtime = get()) }
        single<AppMessagesFeature> { AppMessagesService(runtime = get()) }
        single<AppTextsFeature> { AppTextsService() }
        single {
            AppRootComponent(
                runtime = get(),
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
                onDestroy = onDestroy,
            )
        }
    }
