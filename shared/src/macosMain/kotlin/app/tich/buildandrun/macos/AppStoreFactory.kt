@file:Suppress("unused")

package app.tich.buildandrun.macos

import app.tich.buildandrun.appstore.AppRootComponent
import app.tich.buildandrun.appstore.createAppRootComponent

object AppStoreFactory {
    fun create(): AppRootComponent = createAppRootComponent(graph = MacOSAppGraph())
}
