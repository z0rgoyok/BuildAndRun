package app.tich.buildandrun.presentation.app

data class RepositoryItem(
    val id: String,
    val name: String,
    val path: String,
    val isArchived: Boolean,
    val groupId: String? = null,
    val worktrees: List<WorktreeItem>,
)
