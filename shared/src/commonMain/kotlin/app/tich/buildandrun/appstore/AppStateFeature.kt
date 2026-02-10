package app.tich.buildandrun.appstore

import com.arkivanov.decompose.value.Value

interface AppStateFeature {
    val state: Value<AppStore.State>
}
