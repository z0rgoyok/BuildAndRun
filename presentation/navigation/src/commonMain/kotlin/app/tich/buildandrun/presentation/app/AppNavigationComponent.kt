package app.tich.buildandrun.presentation.app

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

class AppNavigationComponent(
    componentContext: ComponentContext,
) : AppNavigationFeature, ComponentContext by componentContext {
    private val childNavigation = StackNavigation<ChildConfig>()
    private val sheetNavigation = SlotNavigation<SheetConfig>()
    private val mutableState = MutableValue(AppNavigationState())

    override val state: Value<AppNavigationState> = mutableState

    private val childStackValue: Value<ChildStack<ChildConfig, AppChild>> =
        childStack(
            source = childNavigation,
            serializer = null,
            initialConfiguration = ChildConfig.WORKSPACE,
            handleBackButton = false,
            childFactory = ::createChild,
        )

    private val sheetSlotValue: Value<ChildSlot<SheetConfig, AppSheetState>> =
        childSlot(
            source = sheetNavigation,
            serializer = null,
            handleBackButton = true,
            childFactory = ::createSheet,
        )

    init {
        childStackValue.subscribe { stack ->
            mutableState.value =
                mutableState.value.copy(
                    activeChild = stack.active.instance,
                )
        }
        sheetSlotValue.subscribe { slot ->
            mutableState.value =
                mutableState.value.copy(
                    activeSheet = slot.child?.instance,
                )
        }
    }

    override fun onSelectChild(child: AppChild) {
        val target = child.toConfig()
        val current = childStackValue.value.active.configuration
        if (current == target) {
            return
        }
        childNavigation.bringToFront(target)
    }

    override fun onPresentSheet(
        kind: AppSheetKind,
        worktreePath: String?,
    ) {
        val normalizedWorktreePath = worktreePath?.trim()?.ifBlank { null }
        if (kind == AppSheetKind.CREATE_PR || kind == AppSheetKind.COMPLETE_WORKTREE) {
            require(normalizedWorktreePath != null) {
                "worktreePath is required for $kind"
            }
        }
        sheetNavigation.activate(
            kind.toConfig(worktreePath = normalizedWorktreePath),
        )
    }

    override fun onDismissSheet() {
        if (sheetSlotValue.value.child == null) {
            return
        }
        sheetNavigation.dismiss()
    }

    private fun createChild(
        configuration: ChildConfig,
        componentContext: ComponentContext,
    ): AppChild {
        componentContext
        return configuration.toChild()
    }

    private fun createSheet(
        configuration: SheetConfig,
        componentContext: ComponentContext,
    ): AppSheetState {
        componentContext
        return configuration.toSheetState()
    }

    private fun AppChild.toConfig(): ChildConfig =
        when (this) {
            AppChild.WORKSPACE -> ChildConfig.WORKSPACE
            AppChild.SETTINGS -> ChildConfig.SETTINGS
            AppChild.HELP -> ChildConfig.HELP
        }

    private fun ChildConfig.toChild(): AppChild =
        when (this) {
            ChildConfig.WORKSPACE -> AppChild.WORKSPACE
            ChildConfig.SETTINGS -> AppChild.SETTINGS
            ChildConfig.HELP -> AppChild.HELP
        }

    private fun AppSheetKind.toConfig(worktreePath: String?): SheetConfig =
        when (this) {
            AppSheetKind.ADD_REPOSITORY -> SheetConfig.AddRepository
            AppSheetKind.ADD_WORKTREE -> SheetConfig.AddWorktree
            AppSheetKind.CONFIGURE_EDITORS -> SheetConfig.ConfigureEditors
            AppSheetKind.HELP -> SheetConfig.Help
            AppSheetKind.CREATE_PR -> SheetConfig.CreatePullRequest(worktreePath = requireNotNull(worktreePath))
            AppSheetKind.COMPLETE_WORKTREE -> SheetConfig.CompleteWorktree(worktreePath = requireNotNull(worktreePath))
        }

    private fun SheetConfig.toSheetState(): AppSheetState =
        when (this) {
            SheetConfig.AddRepository -> AppSheetState(kind = AppSheetKind.ADD_REPOSITORY)
            SheetConfig.AddWorktree -> AppSheetState(kind = AppSheetKind.ADD_WORKTREE)
            SheetConfig.ConfigureEditors -> AppSheetState(kind = AppSheetKind.CONFIGURE_EDITORS)
            SheetConfig.Help -> AppSheetState(kind = AppSheetKind.HELP)
            is SheetConfig.CreatePullRequest ->
                AppSheetState(
                    kind = AppSheetKind.CREATE_PR,
                    worktreePath = worktreePath,
                )

            is SheetConfig.CompleteWorktree ->
                AppSheetState(
                    kind = AppSheetKind.COMPLETE_WORKTREE,
                    worktreePath = worktreePath,
                )
        }

    private enum class ChildConfig {
        WORKSPACE,
        SETTINGS,
        HELP,
    }

    private sealed interface SheetConfig {
        data object AddRepository : SheetConfig

        data object AddWorktree : SheetConfig

        data object ConfigureEditors : SheetConfig

        data object Help : SheetConfig

        data class CreatePullRequest(
            val worktreePath: String,
        ) : SheetConfig

        data class CompleteWorktree(
            val worktreePath: String,
        ) : SheetConfig
    }
}
