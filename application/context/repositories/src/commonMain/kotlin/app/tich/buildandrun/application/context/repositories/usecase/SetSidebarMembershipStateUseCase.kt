package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult

class SetSidebarMembershipStateUseCase(
    private val preferencesStore: PreferencesStore,
) {
    fun execute(input: Input): UseCaseResult<Output> {
        val next =
            updateSidebarSet(
                current = input.currentIds,
                value = input.id,
                enabled = input.enabled,
            )

        return when (
            val result =
                persistSidebarSetUpdate(
                    current = input.currentIds,
                    next = next,
                    persist = { updated -> persist(target = input.target, value = updated) },
                )
        ) {
            is UseCaseResult.Success -> UseCaseResult.Success(value = Output(ids = result.value))
            is UseCaseResult.Failure -> result
        }
    }

    private fun persist(
        target: Target,
        value: Set<String>,
    ) {
        when (target) {
            Target.EXPANDED_REPOSITORIES -> preferencesStore.expandedRepositoryIds = value
            Target.COLLAPSED_GROUPS -> preferencesStore.collapsedGroupIds = value
        }
    }

    data class Input(
        val target: Target,
        val id: String,
        val enabled: Boolean,
        val currentIds: Set<String>,
    )

    data class Output(
        val ids: Set<String>,
    )

    enum class Target {
        EXPANDED_REPOSITORIES,
        COLLAPSED_GROUPS,
    }
}
