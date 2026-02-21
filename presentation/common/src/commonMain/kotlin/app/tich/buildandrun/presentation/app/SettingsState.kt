package app.tich.buildandrun.presentation.app

data class SettingsState(
    val branches: List<String> = emptyList(),
    val worktreeBasePath: String = "",
    val defaultCopyPatterns: List<String> = emptyList(),
    val selectedRepositoryCustomCopyPatterns: List<String>? = null,
    val selectedRepositoryEffectiveCopyPatterns: List<String> = emptyList(),
)
