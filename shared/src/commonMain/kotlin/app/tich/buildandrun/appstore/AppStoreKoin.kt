package app.tich.buildandrun.appstore

import com.arkivanov.decompose.ComponentContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun appStoreModule(
    graph: AppStoreGraph,
    componentContext: ComponentContext = defaultAppComponentContext(),
    onDestroy: (() -> Unit)? = null,
): Module =
    module {
        single<AppStoreGraph> { graph }
        single<ComponentContext> { componentContext }
        single { AppRuntime(graph = get()) }
        single<AppNavigationFeature> { AppNavigationComponent(componentContext = get()) }
        single<AppStateFeature> {
            AppStateService(
                runtime = get(),
                navigation = get(),
            )
        }
        single<AppRepositoriesFeature> { AppRepositoriesService(runtime = get()) }
        single<AppWorktreesFeature> { AppWorktreesService(runtime = get()) }
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
                stateFeature = get(),
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
