package app.tich.buildandrun.macos

import app.tich.buildandrun.application.ports.PreferencesStore
import app.tich.buildandrun.domain.entities.CopyPattern
import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.entities.RepositoryId
import platform.Foundation.NSUserDefaults

class MacOSPreferencesStore(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : PreferencesStore {
    override suspend fun loadRepositories(): List<Repository> =
        readDictionaryList(key = PREFERENCE_KEY_REPOSITORIES).map { dictionary ->
            val id = dictionary.requiredString(key = REPOSITORY_FIELD_ID)
            val path = dictionary.requiredString(key = REPOSITORY_FIELD_PATH)
            val name = dictionary.requiredString(key = REPOSITORY_FIELD_NAME)
            val isArchived = dictionary.requiredBoolean(key = REPOSITORY_FIELD_IS_ARCHIVED)
            Repository(
                id = RepositoryId(value = id),
                path = path,
                name = name,
                isArchived = isArchived,
            )
        }

    override suspend fun saveRepositories(repositories: List<Repository>) {
        val payload =
            repositories.map { repository ->
                mapOf<String, Any>(
                    REPOSITORY_FIELD_ID to repository.id.value,
                    REPOSITORY_FIELD_PATH to repository.path,
                    REPOSITORY_FIELD_NAME to repository.name,
                    REPOSITORY_FIELD_IS_ARCHIVED to repository.isArchived,
                )
            }
        defaults.setObject(
            value = payload,
            forKey = PREFERENCE_KEY_REPOSITORIES,
        )
    }

    override var worktreeBasePath: String
        get() = defaults.stringForKey(PREFERENCE_KEY_WORKTREE_BASE_PATH) ?: ""
        set(value) {
            defaults.setObject(
                value = value,
                forKey = PREFERENCE_KEY_WORKTREE_BASE_PATH,
            )
        }

    override var expandedRepositoryIds: Set<String>
        get() = readStringList(key = PREFERENCE_KEY_EXPANDED_REPOSITORY_IDS).toSet()
        set(value) {
            defaults.setObject(
                value = value.toList(),
                forKey = PREFERENCE_KEY_EXPANDED_REPOSITORY_IDS,
            )
        }

    override var lastSelectedRepositoryId: String?
        get() = defaults.stringForKey(PREFERENCE_KEY_LAST_SELECTED_REPOSITORY_ID)
        set(value) {
            if (value == null) {
                defaults.removeObjectForKey(defaultName = PREFERENCE_KEY_LAST_SELECTED_REPOSITORY_ID)
                return
            }
            defaults.setObject(
                value = value,
                forKey = PREFERENCE_KEY_LAST_SELECTED_REPOSITORY_ID,
            )
        }

    override var lastSelectedWorktreePath: String?
        get() = defaults.stringForKey(PREFERENCE_KEY_LAST_SELECTED_WORKTREE_PATH)
        set(value) {
            if (value == null) {
                defaults.removeObjectForKey(defaultName = PREFERENCE_KEY_LAST_SELECTED_WORKTREE_PATH)
                return
            }
            defaults.setObject(
                value = value,
                forKey = PREFERENCE_KEY_LAST_SELECTED_WORKTREE_PATH,
            )
        }

    override var rememberEditorChoice: Boolean
        get() {
            if (defaults.objectForKey(defaultName = PREFERENCE_KEY_REMEMBER_EDITOR_CHOICE) == null) {
                return true
            }
            return defaults.boolForKey(defaultName = PREFERENCE_KEY_REMEMBER_EDITOR_CHOICE)
        }
        set(value) {
            defaults.setBool(
                value = value,
                forKey = PREFERENCE_KEY_REMEMBER_EDITOR_CHOICE,
            )
        }

    override fun preferredEditorId(forRepositoryId: RepositoryId): String? =
        readStringMap(key = PREFERENCE_KEY_PREFERRED_EDITOR_IDS)[forRepositoryId.value]

    override fun setPreferredEditorId(
        editorId: String,
        forRepositoryId: RepositoryId,
    ) {
        val map = readStringMap(key = PREFERENCE_KEY_PREFERRED_EDITOR_IDS).toMutableMap()
        map[forRepositoryId.value] = editorId
        writeStringMap(
            key = PREFERENCE_KEY_PREFERRED_EDITOR_IDS,
            value = map,
        )
    }

    override fun removePreferredEditorId(forRepositoryId: RepositoryId) {
        val map = readStringMap(key = PREFERENCE_KEY_PREFERRED_EDITOR_IDS).toMutableMap()
        map.remove(forRepositoryId.value)
        writeStringMap(
            key = PREFERENCE_KEY_PREFERRED_EDITOR_IDS,
            value = map,
        )
    }

    override var enabledEditorIds: Set<String>?
        get() {
            val rawValue = defaults.objectForKey(defaultName = PREFERENCE_KEY_ENABLED_EDITOR_IDS) ?: return null
            return rawValue.asStringList().toSet()
        }
        set(value) {
            if (value == null) {
                defaults.removeObjectForKey(defaultName = PREFERENCE_KEY_ENABLED_EDITOR_IDS)
                return
            }
            defaults.setObject(
                value = value.toList(),
                forKey = PREFERENCE_KEY_ENABLED_EDITOR_IDS,
            )
        }

    override fun isEditorEnabled(editorId: String): Boolean {
        val currentEnabledIds = enabledEditorIds ?: return true
        return currentEnabledIds.contains(element = editorId)
    }

    override fun setEditorEnabled(
        editorId: String,
        enabled: Boolean,
        allEditorIds: List<String>,
    ) {
        val baseSet = (enabledEditorIds ?: allEditorIds.toSet()).toMutableSet()
        if (enabled) {
            baseSet.add(element = editorId)
        } else {
            baseSet.remove(element = editorId)
        }
        enabledEditorIds = baseSet.toSet()
    }

    override fun preferredBaseBranch(forRepositoryId: RepositoryId): String? =
        readStringMap(key = PREFERENCE_KEY_PREFERRED_BASE_BRANCHES)[forRepositoryId.value]

    override fun setPreferredBaseBranch(
        branch: String,
        forRepositoryId: RepositoryId,
    ) {
        val map = readStringMap(key = PREFERENCE_KEY_PREFERRED_BASE_BRANCHES).toMutableMap()
        map[forRepositoryId.value] = branch
        writeStringMap(
            key = PREFERENCE_KEY_PREFERRED_BASE_BRANCHES,
            value = map,
        )
    }

    override fun worktreeBaseBranch(forWorktreePath: String): String? =
        readStringMap(key = PREFERENCE_KEY_WORKTREE_BASE_BRANCHES)[forWorktreePath]

    override fun setWorktreeBaseBranch(
        branch: String,
        forWorktreePath: String,
    ) {
        val map = readStringMap(key = PREFERENCE_KEY_WORKTREE_BASE_BRANCHES).toMutableMap()
        map[forWorktreePath] = branch
        writeStringMap(
            key = PREFERENCE_KEY_WORKTREE_BASE_BRANCHES,
            value = map,
        )
    }

    override fun removeWorktreeBaseBranch(forWorktreePath: String) {
        val map = readStringMap(key = PREFERENCE_KEY_WORKTREE_BASE_BRANCHES).toMutableMap()
        map.remove(forWorktreePath)
        writeStringMap(
            key = PREFERENCE_KEY_WORKTREE_BASE_BRANCHES,
            value = map,
        )
    }

    override var defaultCopyPatterns: List<CopyPattern>
        get() = readStringList(key = PREFERENCE_KEY_DEFAULT_COPY_PATTERNS).map { pattern -> CopyPattern(pattern = pattern) }
        set(value) {
            defaults.setObject(
                value = value.map(CopyPattern::pattern),
                forKey = PREFERENCE_KEY_DEFAULT_COPY_PATTERNS,
            )
        }

    override fun copyPatterns(forRepositoryId: RepositoryId): List<CopyPattern>? =
        readStringListMap(key = PREFERENCE_KEY_REPOSITORY_COPY_PATTERNS)[forRepositoryId.value]?.map { pattern ->
            CopyPattern(pattern = pattern)
        }

    override fun setCopyPatterns(
        patterns: List<CopyPattern>,
        forRepositoryId: RepositoryId,
    ) {
        val map = readStringListMap(key = PREFERENCE_KEY_REPOSITORY_COPY_PATTERNS).toMutableMap()
        map[forRepositoryId.value] = patterns.map(CopyPattern::pattern)
        writeStringListMap(
            key = PREFERENCE_KEY_REPOSITORY_COPY_PATTERNS,
            value = map,
        )
    }

    override fun removeCopyPatterns(forRepositoryId: RepositoryId) {
        val map = readStringListMap(key = PREFERENCE_KEY_REPOSITORY_COPY_PATTERNS).toMutableMap()
        map.remove(forRepositoryId.value)
        writeStringListMap(
            key = PREFERENCE_KEY_REPOSITORY_COPY_PATTERNS,
            value = map,
        )
    }

    override fun effectiveCopyPatterns(forRepositoryId: RepositoryId): List<CopyPattern> =
        copyPatterns(forRepositoryId = forRepositoryId) ?: defaultCopyPatterns

    private fun readDictionaryList(key: String): List<Map<*, *>> {
        val rawValue = defaults.objectForKey(defaultName = key) ?: return emptyList()
        val values = rawValue as? List<*> ?: error("Expected list for key: $key")
        return values.map { value ->
            value as? Map<*, *> ?: error("Expected dictionary in list for key: $key")
        }
    }

    private fun readStringList(key: String): List<String> {
        val rawValue = defaults.objectForKey(defaultName = key) ?: return emptyList()
        return rawValue.asStringList()
    }

    private fun readStringMap(key: String): Map<String, String> {
        val rawValue = defaults.objectForKey(defaultName = key) ?: return emptyMap()
        val map = rawValue as? Map<*, *> ?: error("Expected map for key: $key")
        return map.mapKeys { entry -> entry.key as? String ?: error("Expected string key in map: $key") }
            .mapValues { entry -> entry.value as? String ?: error("Expected string value in map: $key") }
    }

    private fun writeStringMap(
        key: String,
        value: Map<String, String>,
    ) {
        defaults.setObject(
            value = value,
            forKey = key,
        )
    }

    private fun readStringListMap(key: String): Map<String, List<String>> {
        val rawValue = defaults.objectForKey(defaultName = key) ?: return emptyMap()
        val map = rawValue as? Map<*, *> ?: error("Expected map for key: $key")
        return map.mapKeys { entry -> entry.key as? String ?: error("Expected string key in map: $key") }
            .mapValues { entry -> (entry.value ?: error("Expected list value in map: $key")).asStringList() }
    }

    private fun writeStringListMap(
        key: String,
        value: Map<String, List<String>>,
    ) {
        defaults.setObject(
            value = value,
            forKey = key,
        )
    }

    private fun Any.asStringList(): List<String> {
        val values = this as? List<*> ?: error("Expected list value")
        return values.map { value -> value as? String ?: error("Expected string list element") }
    }

    private fun Map<*, *>.requiredString(key: String): String = this[key] as? String ?: error("Expected string field: $key")

    private fun Map<*, *>.requiredBoolean(key: String): Boolean {
        val rawValue = this[key] ?: error("Expected boolean field: $key")
        return when (rawValue) {
            is Boolean -> rawValue
            is Number -> rawValue.toInt() != 0
            else -> error("Expected boolean field: $key")
        }
    }

    private companion object {
        const val PREFERENCE_KEY_REPOSITORIES = "preferences.repositories"
        const val PREFERENCE_KEY_WORKTREE_BASE_PATH = "preferences.worktreeBasePath"
        const val PREFERENCE_KEY_EXPANDED_REPOSITORY_IDS = "preferences.expandedRepositoryIds"
        const val PREFERENCE_KEY_LAST_SELECTED_REPOSITORY_ID = "preferences.lastSelectedRepositoryId"
        const val PREFERENCE_KEY_LAST_SELECTED_WORKTREE_PATH = "preferences.lastSelectedWorktreePath"
        const val PREFERENCE_KEY_REMEMBER_EDITOR_CHOICE = "preferences.rememberEditorChoice"
        const val PREFERENCE_KEY_PREFERRED_EDITOR_IDS = "preferences.preferredEditorIds"
        const val PREFERENCE_KEY_ENABLED_EDITOR_IDS = "preferences.enabledEditorIds"
        const val PREFERENCE_KEY_PREFERRED_BASE_BRANCHES = "preferences.preferredBaseBranches"
        const val PREFERENCE_KEY_WORKTREE_BASE_BRANCHES = "preferences.worktreeBaseBranches"
        const val PREFERENCE_KEY_DEFAULT_COPY_PATTERNS = "preferences.defaultCopyPatterns"
        const val PREFERENCE_KEY_REPOSITORY_COPY_PATTERNS = "preferences.repositoryCopyPatterns"
        const val REPOSITORY_FIELD_ID = "id"
        const val REPOSITORY_FIELD_PATH = "path"
        const val REPOSITORY_FIELD_NAME = "name"
        const val REPOSITORY_FIELD_IS_ARCHIVED = "isArchived"
    }
}
