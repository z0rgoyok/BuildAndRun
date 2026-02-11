package app.tich.buildandrun.application.context.worktrees.usecase

class ReconcileSelectedWorktreePathUseCase {
    fun execute(input: Input): Output {
        val selectedPath =
            input.selectedWorktreePath
                ?: return Output(
                    selectedWorktreePath = null,
                    changed = false,
                )
        if (input.availableWorktreePaths.contains(selectedPath)) {
            return Output(
                selectedWorktreePath = selectedPath,
                changed = false,
            )
        }
        return Output(
            selectedWorktreePath = null,
            changed = true,
        )
    }

    data class Input(
        val selectedWorktreePath: String?,
        val availableWorktreePaths: Set<String>,
    )

    data class Output(
        val selectedWorktreePath: String?,
        val changed: Boolean,
    )
}
