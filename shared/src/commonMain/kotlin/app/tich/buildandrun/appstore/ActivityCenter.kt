package app.tich.buildandrun.appstore

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class ActivityCenter {
    private val globalStack = mutableListOf<Activity>()
    private val worktreeStacks = mutableMapOf<String, MutableList<Activity>>()

    val isGlobalActive: Boolean get() = globalStack.isNotEmpty()
    val currentGlobalMessage: String? get() = globalStack.lastOrNull()?.message

    fun isWorktreeActive(path: String): Boolean =
        worktreeStacks[path]?.isNotEmpty() == true

    @OptIn(ExperimentalUuidApi::class)
    fun beginGlobal(message: String): String {
        val id = Uuid.random().toString()
        globalStack.add(Activity(id = id, message = message))
        return id
    }

    @OptIn(ExperimentalUuidApi::class)
    fun beginWorktree(path: String, message: String): String {
        val id = Uuid.random().toString()
        worktreeStacks.getOrPut(path) { mutableListOf() }.add(Activity(id = id, message = message))
        return id
    }

    fun end(tokenId: String) {
        globalStack.removeAll { it.id == tokenId }
        val emptyPaths = mutableListOf<String>()
        worktreeStacks.forEach { (path, list) ->
            list.removeAll { it.id == tokenId }
            if (list.isEmpty()) emptyPaths.add(path)
        }
        emptyPaths.forEach { worktreeStacks.remove(it) }
    }

    private data class Activity(val id: String, val message: String)
}
