@file:Suppress("unused")

package app.tich.buildandrun.macos

import app.tich.buildandrun.presentation.app.AppRootComponent
import app.tich.buildandrun.presentation.app.core.AppStoreGraph
import app.tich.buildandrun.presentation.app.createAppRootComponent
import org.koin.core.logger.Level
import org.koin.dsl.koinApplication

object AppStoreFactory {
    fun create(): AppRootComponent {
        val graph =
            koinApplication {
                printLogger(Level.NONE)
                modules(macosPlatformModule())
            }.koin.get<AppStoreGraph>()
        return createAppRootComponent(graph = graph)
    }
}
