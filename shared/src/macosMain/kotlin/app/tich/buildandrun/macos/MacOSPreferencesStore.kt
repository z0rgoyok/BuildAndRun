package app.tich.buildandrun.macos

import app.tich.buildandrun.application.ports.PreferencesStore
import app.tich.buildandrun.domain.entities.*
import platform.Foundation.NSUserDefaults

class MacOSPreferencesStore(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : PreferencesStore {
    override suspend fun loadRepositories(): List<Repository> =
        defaults.readDictionaryList(key = PREFERENCE_KEY_REPOSITORIES).map { dictionary ->
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
        get() = defaults.readStringList(key = PREFERENCE_KEY_EXPANDED_REPOSITORY_IDS).toSet()
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
        defaults.readStringMap(key = PREFERENCE_KEY_PREFERRED_EDITOR_IDS)[forRepositoryId.value]

    override fun setPreferredEditorId(
        editorId: String,
        forRepositoryId: RepositoryId,
    ) {
        val map = defaults.readStringMap(key = PREFERENCE_KEY_PREFERRED_EDITOR_IDS).toMutableMap()
        map[forRepositoryId.value] = editorId
        defaults.writeStringMap(
            key = PREFERENCE_KEY_PREFERRED_EDITOR_IDS,
            value = map,
        )
    }

    override fun removePreferredEditorId(forRepositoryId: RepositoryId) {
        val map = defaults.readStringMap(key = PREFERENCE_KEY_PREFERRED_EDITOR_IDS).toMutableMap()
        map.remove(forRepositoryId.value)
        defaults.writeStringMap(
            key = PREFERENCE_KEY_PREFERRED_EDITOR_IDS,
            value = map,
        )
    }

    override var enabledEditorIds: Set<String>?
        get() = defaults.readStringListOrNull(key = PREFERENCE_KEY_ENABLED_EDITOR_IDS)?.toSet()
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
        defaults.readStringMap(key = PREFERENCE_KEY_PREFERRED_BASE_BRANCHES)[forRepositoryId.value]

    override fun setPreferredBaseBranch(
        branch: String,
        forRepositoryId: RepositoryId,
    ) {
        val map = defaults.readStringMap(key = PREFERENCE_KEY_PREFERRED_BASE_BRANCHES).toMutableMap()
        map[forRepositoryId.value] = branch
        defaults.writeStringMap(
            key = PREFERENCE_KEY_PREFERRED_BASE_BRANCHES,
            value = map,
        )
    }

    override fun worktreeBaseBranch(forWorktreePath: String): String? =
        defaults.readStringMap(key = PREFERENCE_KEY_WORKTREE_BASE_BRANCHES)[forWorktreePath]

    override fun setWorktreeBaseBranch(
        branch: String,
        forWorktreePath: String,
    ) {
        val map = defaults.readStringMap(key = PREFERENCE_KEY_WORKTREE_BASE_BRANCHES).toMutableMap()
        map[forWorktreePath] = branch
        defaults.writeStringMap(
            key = PREFERENCE_KEY_WORKTREE_BASE_BRANCHES,
            value = map,
        )
    }

    override fun removeWorktreeBaseBranch(forWorktreePath: String) {
        val map = defaults.readStringMap(key = PREFERENCE_KEY_WORKTREE_BASE_BRANCHES).toMutableMap()
        map.remove(forWorktreePath)
        defaults.writeStringMap(
            key = PREFERENCE_KEY_WORKTREE_BASE_BRANCHES,
            value = map,
        )
    }

    override var defaultCopyPatterns: List<CopyPattern>
        get() = defaults.readStringList(key = PREFERENCE_KEY_DEFAULT_COPY_PATTERNS).map { pattern -> CopyPattern(pattern = pattern) }
        set(value) {
            defaults.setObject(
                value = value.map(CopyPattern::pattern),
                forKey = PREFERENCE_KEY_DEFAULT_COPY_PATTERNS,
            )
        }

    override fun copyPatterns(forRepositoryId: RepositoryId): List<CopyPattern>? =
        defaults.readStringListMap(key = PREFERENCE_KEY_REPOSITORY_COPY_PATTERNS)[forRepositoryId.value]?.map { pattern ->
            CopyPattern(pattern = pattern)
        }

    override fun setCopyPatterns(
        patterns: List<CopyPattern>,
        forRepositoryId: RepositoryId,
    ) {
        val map = defaults.readStringListMap(key = PREFERENCE_KEY_REPOSITORY_COPY_PATTERNS).toMutableMap()
        map[forRepositoryId.value] = patterns.map(CopyPattern::pattern)
        defaults.writeStringListMap(
            key = PREFERENCE_KEY_REPOSITORY_COPY_PATTERNS,
            value = map,
        )
    }

    override fun removeCopyPatterns(forRepositoryId: RepositoryId) {
        val map = defaults.readStringListMap(key = PREFERENCE_KEY_REPOSITORY_COPY_PATTERNS).toMutableMap()
        map.remove(forRepositoryId.value)
        defaults.writeStringListMap(
            key = PREFERENCE_KEY_REPOSITORY_COPY_PATTERNS,
            value = map,
        )
    }

    override fun effectiveCopyPatterns(forRepositoryId: RepositoryId): List<CopyPattern> =
        copyPatterns(forRepositoryId = forRepositoryId) ?: defaultCopyPatterns

    override fun loadKanbanTasks(forRepositoryId: RepositoryId): List<KanbanTask> {
        val entries = defaults.readKanbanTaskMap()[forRepositoryId.value].orEmpty()
        return entries.mapNotNull { entry ->
            val id = entry[KANBAN_TASK_FIELD_ID] as? String ?: return@mapNotNull null
            val title = entry[KANBAN_TASK_FIELD_TITLE] as? String ?: return@mapNotNull null
            if (title.isBlank()) {
                return@mapNotNull null
            }
            val column =
                when (val rawColumn = entry[KANBAN_TASK_FIELD_COLUMN]) {
                    is String -> KanbanColumnType.entries.firstOrNull { it.name == rawColumn } ?: return@mapNotNull null
                    else -> return@mapNotNull null
                }
            val createdAt =
                when (val rawCreatedAt = entry[KANBAN_TASK_FIELD_CREATED_AT]) {
                    is Number -> rawCreatedAt.toLong()
                    else -> return@mapNotNull null
                }
            val order =
                when (val rawOrder = entry[KANBAN_TASK_FIELD_ORDER]) {
                    is Number -> rawOrder.toInt()
                    else -> return@mapNotNull null
                }
            val description = entry[KANBAN_TASK_FIELD_DESCRIPTION] as? String
            KanbanTask(
                id = KanbanTaskId(value = id),
                title = title,
                description = description?.ifBlank { null },
                columnId = column,
                worktreePath = null,
                createdAt = createdAt,
                order = order,
            )
        }
    }

    override fun setKanbanTasks(
        tasks: List<KanbanTask>,
        forRepositoryId: RepositoryId,
    ) {
        val map = defaults.readKanbanTaskMap().toMutableMap()
        map[forRepositoryId.value] =
            tasks.map { task ->
                mapOf<String, Any>(
                    KANBAN_TASK_FIELD_ID to task.id.value,
                    KANBAN_TASK_FIELD_TITLE to task.title,
                    KANBAN_TASK_FIELD_COLUMN to task.columnId.name,
                    KANBAN_TASK_FIELD_CREATED_AT to task.createdAt,
                    KANBAN_TASK_FIELD_ORDER to task.order,
                ).let { payload ->
                    val description = task.description
                    if (description == null) {
                        payload
                    } else {
                        payload + mapOf(KANBAN_TASK_FIELD_DESCRIPTION to description)
                    }
                }
            }
        defaults.writeKanbanTaskMap(value = map)
    }

    override fun removeKanbanTasks(forRepositoryId: RepositoryId) {
        val map = defaults.readKanbanTaskMap().toMutableMap()
        map.remove(forRepositoryId.value)
        defaults.writeKanbanTaskMap(value = map)
    }
}
