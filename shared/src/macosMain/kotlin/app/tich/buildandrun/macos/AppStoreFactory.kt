@file:Suppress("unused")

package app.tich.buildandrun.macos

import app.tich.buildandrun.appstore.AppStore

object AppStoreFactory {
    fun create(): AppStore = AppStore(graph = MacOSAppGraph())
}
