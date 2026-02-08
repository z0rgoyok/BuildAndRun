package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.entities.CopyPattern
import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.entities.RepositoryId
import app.tich.buildandrun.domain.ports.PreferencesStore

class InMemoryPreferencesStore : PreferencesStore {
    private var repositoriesStorage = mutableListOf<Repository>()
    private val preferredEditors = mutableMapOf<String, String>()
    private val preferredBranches = mutableMapOf<String, String>()
    private val worktreeBaseBranches = mutableMapOf<String, String>()
    private val repositoryCopyPatterns = mutableMapOf<String, List<CopyPattern>>()

    override suspend fun loadRepositories(): List<Repository> = repositoriesStorage.toList()

    override suspend fun saveRepositories(repositories: List<Repository>) {
        repositoriesStorage = repositories.toMutableList()
    }

    override var worktreeBasePath: String = ""
    override var expandedRepositoryIds: Set<String> = emptySet()
    override var lastSelectedRepositoryId: String? = null
    override var lastSelectedWorktreePath: String? = null
    override var rememberEditorChoice: Boolean = true
    override var enabledEditorIds: Set<String>? = null
    override var defaultCopyPatterns: List<CopyPattern> = emptyList()

    override fun preferredEditorId(forRepositoryId: RepositoryId): String? = preferredEditors[forRepositoryId.value]

    override fun setPreferredEditorId(
        editorId: String,
        forRepositoryId: RepositoryId,
    ) {
        preferredEditors[forRepositoryId.value] = editorId
    }

    override fun removePreferredEditorId(forRepositoryId: RepositoryId) {
        preferredEditors.remove(forRepositoryId.value)
    }

    override fun isEditorEnabled(editorId: String): Boolean {
        val currentEnabledIds = enabledEditorIds ?: return true
        return currentEnabledIds.contains(editorId)
    }

    override fun setEditorEnabled(
        editorId: String,
        enabled: Boolean,
        allEditorIds: List<String>,
    ) {
        val baseSet = (enabledEditorIds ?: allEditorIds.toSet()).toMutableSet()
        if (enabled) {
            baseSet.add(editorId)
        } else {
            baseSet.remove(editorId)
        }
        enabledEditorIds = baseSet.toSet()
    }

    override fun preferredBaseBranch(forRepositoryId: RepositoryId): String? = preferredBranches[forRepositoryId.value]

    override fun setPreferredBaseBranch(
        branch: String,
        forRepositoryId: RepositoryId,
    ) {
        preferredBranches[forRepositoryId.value] = branch
    }

    override fun worktreeBaseBranch(forWorktreePath: String): String? = worktreeBaseBranches[forWorktreePath]

    override fun setWorktreeBaseBranch(
        branch: String,
        forWorktreePath: String,
    ) {
        worktreeBaseBranches[forWorktreePath] = branch
    }

    override fun removeWorktreeBaseBranch(forWorktreePath: String) {
        worktreeBaseBranches.remove(forWorktreePath)
    }

    override fun copyPatterns(forRepositoryId: RepositoryId): List<CopyPattern>? = repositoryCopyPatterns[forRepositoryId.value]

    override fun setCopyPatterns(
        patterns: List<CopyPattern>,
        forRepositoryId: RepositoryId,
    ) {
        repositoryCopyPatterns[forRepositoryId.value] = patterns
    }

    override fun removeCopyPatterns(forRepositoryId: RepositoryId) {
        repositoryCopyPatterns.remove(forRepositoryId.value)
    }

    override fun effectiveCopyPatterns(forRepositoryId: RepositoryId): List<CopyPattern> =
        repositoryCopyPatterns[forRepositoryId.value] ?: defaultCopyPatterns
}
