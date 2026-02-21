package app.tich.buildandrun.application.context.repositories.port

import app.tich.buildandrun.domain.context.copy.model.CopyPattern
import app.tich.buildandrun.domain.context.kanban.model.KanbanTask
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.context.repositories.model.RepositoryGroup
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId

interface PreferencesStore {
    suspend fun loadRepositories(): List<Repository>

    suspend fun saveRepositories(repositories: List<Repository>)

    suspend fun loadRepositoryGroups(): List<RepositoryGroup>

    suspend fun saveRepositoryGroups(groups: List<RepositoryGroup>)

    var worktreeBasePath: String

    var expandedRepositoryIds: Set<String>

    var collapsedGroupIds: Set<String>

    var lastSelectedRepositoryId: String?

    var lastSelectedWorktreePath: String?

    var rememberEditorChoice: Boolean

    fun preferredEditorId(forRepositoryId: RepositoryId): String?

    fun setPreferredEditorId(
        editorId: String,
        forRepositoryId: RepositoryId,
    )

    fun removePreferredEditorId(forRepositoryId: RepositoryId)

    var enabledEditorIds: Set<String>?

    fun isEditorEnabled(editorId: String): Boolean

    fun setEditorEnabled(
        editorId: String,
        enabled: Boolean,
        allEditorIds: List<String>,
    )

    fun preferredBaseBranch(forRepositoryId: RepositoryId): String?

    fun setPreferredBaseBranch(
        branch: String,
        forRepositoryId: RepositoryId,
    )

    fun worktreeBaseBranch(forWorktreePath: String): String?

    fun setWorktreeBaseBranch(
        branch: String,
        forWorktreePath: String,
    )

    fun removeWorktreeBaseBranch(forWorktreePath: String)

    var defaultCopyPatterns: List<CopyPattern>

    fun copyPatterns(forRepositoryId: RepositoryId): List<CopyPattern>?

    fun setCopyPatterns(
        patterns: List<CopyPattern>,
        forRepositoryId: RepositoryId,
    )

    fun removeCopyPatterns(forRepositoryId: RepositoryId)

    fun effectiveCopyPatterns(forRepositoryId: RepositoryId): List<CopyPattern>

    fun loadKanbanTasks(forRepositoryId: RepositoryId): List<KanbanTask>

    fun setKanbanTasks(
        tasks: List<KanbanTask>,
        forRepositoryId: RepositoryId,
    )

    fun removeKanbanTasks(forRepositoryId: RepositoryId)
}
