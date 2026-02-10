package app.tich.buildandrun.presentation.app

import com.arkivanov.decompose.value.Value

interface AppStateFeature {
    val state: Value<AppStore.State>
}
