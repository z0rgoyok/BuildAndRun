package app.tich.buildandrun.presentation.app

import app.tich.buildandrun.presentation.app.core.AppRuntime
import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

internal class AppStateService(
    runtime: AppRuntime,
    private val navigation: AppNavigationFeature,
) : AppStateFeature {
    private val mutableState = MutableValue(mergedState(runtime = runtime))
    private val subscriptions = mutableListOf<Cancellation>()

    override val state: Value<AppStore.State> = mutableState

    init {
        subscriptions += runtime.state.subscribe { publishState(runtime = runtime) }
        subscriptions += navigation.state.subscribe { publishState(runtime = runtime) }
        publishState(runtime = runtime)
    }

    fun destroy() {
        subscriptions.forEach(Cancellation::cancel)
        subscriptions.clear()
    }

    private fun mergedState(runtime: AppRuntime): AppStore.State =
        runtime.state.value.copy(
            activeChild = navigation.state.value.activeChild,
            activeSheet = navigation.state.value.activeSheet,
        )

    private fun publishState(runtime: AppRuntime) {
        mutableState.value = mergedState(runtime = runtime)
    }
}
