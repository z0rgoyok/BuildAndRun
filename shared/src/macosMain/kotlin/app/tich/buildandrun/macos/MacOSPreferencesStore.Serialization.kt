package app.tich.buildandrun.macos

import platform.Foundation.NSUserDefaults

internal const val PREFERENCE_KEY_REPOSITORIES = "preferences.repositories"
internal const val PREFERENCE_KEY_WORKTREE_BASE_PATH = "preferences.worktreeBasePath"
internal const val PREFERENCE_KEY_EXPANDED_REPOSITORY_IDS = "preferences.expandedRepositoryIds"
internal const val PREFERENCE_KEY_LAST_SELECTED_REPOSITORY_ID = "preferences.lastSelectedRepositoryId"
internal const val PREFERENCE_KEY_LAST_SELECTED_WORKTREE_PATH = "preferences.lastSelectedWorktreePath"
internal const val PREFERENCE_KEY_REMEMBER_EDITOR_CHOICE = "preferences.rememberEditorChoice"
internal const val PREFERENCE_KEY_PREFERRED_EDITOR_IDS = "preferences.preferredEditorIds"
internal const val PREFERENCE_KEY_ENABLED_EDITOR_IDS = "preferences.enabledEditorIds"
internal const val PREFERENCE_KEY_PREFERRED_BASE_BRANCHES = "preferences.preferredBaseBranches"
internal const val PREFERENCE_KEY_WORKTREE_BASE_BRANCHES = "preferences.worktreeBaseBranches"
internal const val PREFERENCE_KEY_DEFAULT_COPY_PATTERNS = "preferences.defaultCopyPatterns"
internal const val PREFERENCE_KEY_REPOSITORY_COPY_PATTERNS = "preferences.repositoryCopyPatterns"
internal const val PREFERENCE_KEY_REPOSITORY_KANBAN_TASKS = "preferences.repositoryKanbanTasks"

internal const val REPOSITORY_FIELD_ID = "id"
internal const val REPOSITORY_FIELD_PATH = "path"
internal const val REPOSITORY_FIELD_NAME = "name"
internal const val REPOSITORY_FIELD_IS_ARCHIVED = "isArchived"

internal const val KANBAN_TASK_FIELD_ID = "id"
internal const val KANBAN_TASK_FIELD_TITLE = "title"
internal const val KANBAN_TASK_FIELD_DESCRIPTION = "description"
internal const val KANBAN_TASK_FIELD_COLUMN = "columnId"
internal const val KANBAN_TASK_FIELD_CREATED_AT = "createdAt"
internal const val KANBAN_TASK_FIELD_ORDER = "order"

internal fun NSUserDefaults.readDictionaryList(key: String): List<Map<*, *>> {
    val rawValue = objectForKey(defaultName = key) ?: return emptyList()
    val values = rawValue as? List<*> ?: error("Expected list for key: $key")
    return values.map { value ->
        value as? Map<*, *> ?: error("Expected dictionary in list for key: $key")
    }
}

internal fun NSUserDefaults.readStringList(key: String): List<String> = readStringListOrNull(key = key).orEmpty()

internal fun NSUserDefaults.readStringListOrNull(key: String): List<String>? {
    val rawValue = objectForKey(defaultName = key) ?: return null
    val values = rawValue.asStringListOrNull()
    if (values == null) {
        removeObjectForKey(defaultName = key)
    }
    return values
}

internal fun NSUserDefaults.readStringMap(key: String): Map<String, String> {
    val rawValue = objectForKey(defaultName = key) ?: return emptyMap()
    val map =
        (rawValue as? Map<*, *>) ?: return emptyMap<String, String>().also {
            removeObjectForKey(defaultName = key)
        }
    val parsed = mutableMapOf<String, String>()
    map.forEach { (entryKey, entryValue) ->
        val parsedKey = entryKey as? String ?: return@forEach
        val parsedValue = entryValue as? String ?: return@forEach
        parsed[parsedKey] = parsedValue
    }
    if (parsed.size != map.size) {
        writeStringMap(key = key, value = parsed)
    }
    return parsed
}

internal fun NSUserDefaults.writeStringMap(
    key: String,
    value: Map<String, String>,
) {
    setObject(
        value = value,
        forKey = key,
    )
}

internal fun NSUserDefaults.readStringListMap(key: String): Map<String, List<String>> {
    val rawValue = objectForKey(defaultName = key) ?: return emptyMap()
    val map =
        (rawValue as? Map<*, *>) ?: return emptyMap<String, List<String>>().also {
            removeObjectForKey(defaultName = key)
        }
    val parsed = mutableMapOf<String, List<String>>()
    map.forEach { (entryKey, entryValue) ->
        val parsedKey = entryKey as? String ?: return@forEach
        val parsedValue = entryValue?.asStringListOrNull() ?: return@forEach
        parsed[parsedKey] = parsedValue
    }
    if (parsed.size != map.size) {
        writeStringListMap(key = key, value = parsed)
    }
    return parsed
}

internal fun NSUserDefaults.writeStringListMap(
    key: String,
    value: Map<String, List<String>>,
) {
    setObject(
        value = value,
        forKey = key,
    )
}

internal fun NSUserDefaults.readKanbanTaskMap(): Map<String, List<Map<String, Any>>> {
    val rawValue = objectForKey(defaultName = PREFERENCE_KEY_REPOSITORY_KANBAN_TASKS) ?: return emptyMap()
    val map =
        rawValue as? Map<*, *> ?: return emptyMap<String, List<Map<String, Any>>>().also {
            removeObjectForKey(defaultName = PREFERENCE_KEY_REPOSITORY_KANBAN_TASKS)
        }
    val parsed = mutableMapOf<String, List<Map<String, Any>>>()
    map.forEach { (entryKey, entryValue) ->
        val parsedKey = entryKey as? String ?: return@forEach
        val parsedTasks = readKanbanTaskList(rawValue = entryValue) ?: return@forEach
        parsed[parsedKey] = parsedTasks
    }
    if (parsed.size != map.size) {
        writeKanbanTaskMap(value = parsed)
    }
    return parsed
}

internal fun NSUserDefaults.writeKanbanTaskMap(value: Map<String, List<Map<String, Any>>>) {
    setObject(
        value = value,
        forKey = PREFERENCE_KEY_REPOSITORY_KANBAN_TASKS,
    )
}

internal fun Map<*, *>.requiredString(key: String): String = this[key] as? String ?: error("Expected string field: $key")

internal fun Map<*, *>.requiredBoolean(key: String): Boolean {
    val rawValue = this[key] ?: error("Expected boolean field: $key")
    return when (rawValue) {
        is Boolean -> rawValue
        is Number -> rawValue.toInt() != 0
        else -> error("Expected boolean field: $key")
    }
}

private fun readKanbanTaskList(rawValue: Any?): List<Map<String, Any>>? {
    val values = rawValue as? List<*> ?: return null
    val parsed =
        values.mapNotNull { entry ->
            val taskMap = entry as? Map<*, *> ?: return@mapNotNull null
            val normalized = mutableMapOf<String, Any>()
            taskMap.forEach { (taskKey, taskValue) ->
                val key = taskKey as? String ?: return@forEach
                val value = taskValue ?: return@forEach
                normalized[key] = value
            }
            if (normalized.size != taskMap.size) {
                return@mapNotNull null
            }
            normalized.toMap()
        }
    if (parsed.size != values.size) {
        return null
    }
    return parsed
}

private fun Any.asStringListOrNull(): List<String>? {
    val values = this as? List<*> ?: return null
    val parsed = values.mapNotNull { value -> value as? String }
    if (parsed.size != values.size) {
        return null
    }
    return parsed
}
