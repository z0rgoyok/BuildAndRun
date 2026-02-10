package app.tich.buildandrun.presentation.app.context.state

import app.tich.buildandrun.presentation.app.ActivityState
import app.tich.buildandrun.presentation.app.core.ActivityCenter
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

class ActivityContextState {
    private val mutableState = MutableValue(ActivityState())

    val activityCenter = ActivityCenter()
    val state: Value<ActivityState> = mutableState

    fun publish() {
        mutableState.value =
            ActivityState(
                isLoading = activityCenter.isGlobalActive,
                loadingMessage = activityCenter.currentGlobalMessage,
            )
    }
}
