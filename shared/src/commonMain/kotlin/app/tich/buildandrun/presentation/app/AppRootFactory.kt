package app.tich.buildandrun.presentation.app

import app.tich.buildandrun.presentation.app.core.AppStoreGraph
import com.arkivanov.decompose.ComponentContext
import org.koin.core.KoinApplication
import org.koin.core.logger.Level
import org.koin.dsl.koinApplication

internal fun createAppRootComponent(
    graph: AppStoreGraph,
    componentContext: ComponentContext = defaultAppComponentContext(),
): AppRootComponent {
    var application: KoinApplication? = null
    application =
        koinApplication {
            printLogger(Level.NONE)
            modules(
                appStoreModule(
                    graph = graph,
                    componentContext = componentContext,
                    onDestroy = {
                        application?.close()
                    },
                ),
            )
        }
    return application.koin.get()
}
