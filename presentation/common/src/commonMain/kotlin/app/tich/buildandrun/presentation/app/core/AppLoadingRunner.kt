package app.tich.buildandrun.presentation.app.core

import app.tich.buildandrun.presentation.app.context.state.MessagesContextState
import app.tich.buildandrun.presentation.i18n.UiText
import org.jetbrains.compose.resources.StringResource

class AppLoadingRunner(
    private val stateRefresher: AppStateRefresher,
    private val messagesState: MessagesContextState,
) {
    suspend fun <T> withGlobalLoading(
        resource: StringResource,
        block: suspend () -> T,
    ): T {
        val tokenId = stateRefresher.beginGlobalLoading(resolveText(UiText(resource)))
        messagesState.clear()
        stateRefresher.publishAll()
        try {
            return block()
        } finally {
            stateRefresher.endLoading(tokenId)
            stateRefresher.publishAll()
        }
    }

    suspend fun <T> withWorktreeLoading(
        worktreePath: String,
        resource: StringResource,
        block: suspend () -> T,
    ): T {
        val tokenId = stateRefresher.beginWorktreeLoading(path = worktreePath, message = resolveText(UiText(resource)))
        messagesState.clear()
        stateRefresher.publishAll()
        try {
            return block()
        } finally {
            stateRefresher.endLoading(tokenId)
            stateRefresher.publishAll()
        }
    }
}
