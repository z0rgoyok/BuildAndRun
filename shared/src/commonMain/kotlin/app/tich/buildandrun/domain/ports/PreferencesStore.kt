package app.tich.buildandrun.domain.ports

import app.tich.buildandrun.domain.entities.CopyPattern
import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.entities.RepositoryId

interface PreferencesStore {
    suspend fun loadRepositories(): List<Repository>

    suspend fun saveRepositories(repositories: List<Repository>)

    var worktreeBasePath: String

    var expandedRepositoryIds: Set<String>

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
}
