package app.tich.buildandrun.presentation.app

import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.port.FileSystemHandling
import app.tich.buildandrun.application.context.shared.port.SystemOpening
import app.tich.buildandrun.domain.context.editors.model.Editor
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.context.worktrees.model.Worktree
import app.tich.buildandrun.presentation.app.core.AppGraph
import app.tich.buildandrun.presentation.i18n.UiTextLocalizer
import app.tich.buildandrun.testsupport.FakeGitClient
import app.tich.buildandrun.testsupport.FakePreferencesStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.fail

class AppRootWorktreePruneSelectionTest {
    @Test
    fun onPruneWorktrees_cleansPersistedSelectionWhenSelectedWorktreePruned() =
        runBlocking {
            withTestLocalizer {
                val repository = Repository.create(path = "/tmp/repo-prune")
                val mainWorktree =
                    Worktree(
                        path = "/tmp/repo-prune-main",
                        branch = "main",
                        isMain = true,
                        commitHash = "a1",
                        isLocked = false,
                        isPrunable = false,
                        baseBranch = "main",
                    )
                val prunableWorktree =
                    Worktree(
                        path = "/tmp/repo-prune-feature",
                        branch = "feature/prunable",
                        isMain = false,
                        commitHash = "b2",
                        isLocked = false,
                        isPrunable = true,
                        baseBranch = "main",
                    )
                val preferences = FakePreferencesStore(initialRepositories = listOf(repository))
                preferences.lastSelectedRepositoryId = repository.id.value
                preferences.lastSelectedWorktreePath = prunableWorktree.path
                val gitClient =
                    FakeGitClient().apply {
                        registerRepository(path = repository.path)
                        setWorktrees(
                            repositoryPath = repository.path,
                            worktrees = listOf(mainWorktree, prunableWorktree),
                        )
                    }

                val store = createAppRootComponent(graph = TestGraph(preferencesStore = preferences, gitClient = gitClient))
                waitForRepositories(store = store, expectedCount = 1)
                waitForWorktreeCount(store = store, repositoryId = repository.id.value, expectedCount = 2)
                waitForSelectedWorktree(store = store, expectedPath = prunableWorktree.path)

                store.gitActions.onPruneWorktrees()

                waitForWorktreeCount(store = store, repositoryId = repository.id.value, expectedCount = 1)
                waitForSelectedWorktree(store = store, expectedPath = null)
                waitForPersistedSelection(preferences = preferences, expectedPath = null)
                assertNull(preferences.lastSelectedWorktreePath)
                assertNull(store.worktreesState.value.selectedWorktreePath)

                store.destroy()
            }
        }

    private suspend fun withTestLocalizer(block: suspend () -> Unit) {
        UiTextLocalizer.setResolverOverride { "test" }
        try {
            block()
        } finally {
            UiTextLocalizer.setResolverOverride(null)
        }
    }

    private suspend fun waitForRepositories(
        store: AppRootComponent,
        expectedCount: Int,
    ) {
        repeat(200) {
            if (store.repositoriesState.value.repositories.size == expectedCount) {
                return
            }
            delay(10)
        }
        fail("App root did not load repositories in time")
    }

    private suspend fun waitForWorktreeCount(
        store: AppRootComponent,
        repositoryId: String,
        expectedCount: Int,
    ) {
        repeat(300) {
            val count =
                store.repositoriesState.value.repositories
                    .firstOrNull { it.id == repositoryId }
                    ?.worktrees
                    ?.size
            if (count == expectedCount) {
                return
            }
            delay(10)
        }
        fail("Repository worktrees were not updated in time")
    }

    private suspend fun waitForSelectedWorktree(
        store: AppRootComponent,
        expectedPath: String?,
    ) {
        repeat(300) {
            if (store.worktreesState.value.selectedWorktreePath == expectedPath) {
                return
            }
            delay(10)
        }
        fail("Selected worktree path was not updated in time")
    }

    private suspend fun waitForPersistedSelection(
        preferences: FakePreferencesStore,
        expectedPath: String?,
    ) {
        repeat(300) {
            if (preferences.lastSelectedWorktreePath == expectedPath) {
                return
            }
            delay(10)
        }
        fail("Persisted worktree path was not updated in time")
    }

    private class TestGraph(
        override val preferencesStore: FakePreferencesStore,
        override val gitClient: FakeGitClient,
    ) : AppGraph {
        override val fileSystem = NoOpFileSystemHandling
        override val editorOpening = NoOpEditorOpening
        override val systemOpening = NoOpSystemOpening
    }

    private object NoOpEditorOpening : EditorOpening {
        override suspend fun open(
            path: String,
            withEditor: Editor,
        ) = Unit

        override fun availableEditors(): List<Editor> = emptyList()

        override fun allEditors(): List<Editor> = emptyList()

        override fun isInstalled(editor: Editor): Boolean = false
    }

    private object NoOpSystemOpening : SystemOpening {
        override fun openURL(url: String) = Unit

        override fun revealInFinder(path: String) = Unit

        override fun openTerminal(atPath: String) = Unit
    }

    private object NoOpFileSystemHandling : FileSystemHandling {
        override suspend fun fileExists(atPath: String): Boolean = false

        override suspend fun isDirectory(atPath: String): Boolean = false

        override suspend fun createDirectory(
            atPath: String,
            withIntermediateDirectories: Boolean,
        ) = Unit

        override suspend fun copyItem(
            atPath: String,
            toPath: String,
        ) = Unit

        override suspend fun fileSize(atPath: String): Long? = null

        override suspend fun directorySize(atPath: String): Long? = null

        override suspend fun delete(
            atPath: String,
            recursive: Boolean,
        ) = Unit

        override suspend fun listDirectory(atPath: String): List<String> = emptyList()

        override fun homeDirectory(): String = "/tmp"
    }
}
