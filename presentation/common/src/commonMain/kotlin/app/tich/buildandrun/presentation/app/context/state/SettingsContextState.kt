package app.tich.buildandrun.presentation.app.context.state

import app.tich.buildandrun.domain.context.copy.model.CopyPattern
import app.tich.buildandrun.presentation.app.SettingsState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

class SettingsContextState {
    private val mutableState = MutableValue(SettingsState())

    var branches: List<String> = emptyList()
    var worktreeBasePath: String = ""
    var defaultCopyPatterns: List<CopyPattern> = emptyList()
    var selectedRepositoryCustomCopyPatterns: List<String>? = null
    var selectedRepositoryEffectiveCopyPatterns: List<String> = emptyList()

    val state: Value<SettingsState> = mutableState

    fun publish() {
        mutableState.value =
            SettingsState(
                branches = branches,
                worktreeBasePath = worktreeBasePath,
                defaultCopyPatterns = defaultCopyPatterns.map(CopyPattern::pattern),
                selectedRepositoryCustomCopyPatterns = selectedRepositoryCustomCopyPatterns,
                selectedRepositoryEffectiveCopyPatterns = selectedRepositoryEffectiveCopyPatterns,
            )
    }
}
