package app.tich.buildandrun.presentation.app

import app.tich.buildandrun.application.context.repositories.usecase.*
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.port.FileSystemHandling
import app.tich.buildandrun.application.context.shared.port.SystemOpening
import app.tich.buildandrun.application.context.worktrees.usecase.CreateWorktreeUseCase
import app.tich.buildandrun.application.context.worktrees.usecase.LoadBranchesUseCase
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
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

class AppRootBootstrapCleanupTest {
    @Test
    fun start_cleansUpInvalidPersistedWorktreeSelection() =
        runBlocking {
            withTestLocalizer {
                val repository = Repository.create(path = "/tmp/repo-bootstrap")
                val preferences = FakePreferencesStore(initialRepositories = listOf(repository))
                val gitClient =
                    FakeGitClient().apply {
                        registerRepository(path = repository.path)
                        setWorktrees(
                            repositoryPath = repository.path,
                            worktrees =
                                listOf(
                                    Worktree(
                                        path = "/tmp/repo-bootstrap-valid",
                                        branch = "feature/valid",
                                        isMain = false,
                                        commitHash = "abc",
                                        isLocked = false,
                                        isPrunable = false,
                                        baseBranch = "main",
                                    ),
                                ),
                        )
                    }
                preferences.lastSelectedRepositoryId = repository.id.value
                preferences.lastSelectedWorktreePath = "/tmp/repo-bootstrap-missing"

                val store = createAppRootComponent(graph = TestGraph(preferencesStore = preferences, gitClient = gitClient))

                waitForRepositories(store = store, expectedCount = 1)
                waitForSelectionCleanup(preferences = preferences)

                assertEquals(repository.id.value, store.repositoriesState.value.selectedRepositoryId)
                assertNull(store.worktreesState.value.selectedWorktreePath)
                assertNull(preferences.lastSelectedWorktreePath)

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

    private suspend fun waitForSelectionCleanup(preferences: FakePreferencesStore) {
        repeat(200) {
            if (preferences.lastSelectedWorktreePath == null) {
                return
            }
            delay(10)
        }
        fail("Persisted worktree selection was not cleaned up")
    }

    private class TestGraph(
        override val preferencesStore: FakePreferencesStore,
        override val gitClient: FakeGitClient,
    ) : AppGraph {
        override val fileSystem = NoOpFileSystemHandling
        override val editorOpening = NoOpEditorOpening
        override val systemOpening = NoOpSystemOpening
        override val addRepositoryUseCase = AddRepositoryUseCase(gitClient = gitClient, preferencesStore = preferencesStore)
        override val loadRepositoriesUseCase = LoadRepositoriesUseCase(preferencesStore = preferencesStore)
        override val createWorktreeUseCase = CreateWorktreeUseCase(gitClient = gitClient)
        override val removeRepositoryUseCase = RemoveRepositoryUseCase(preferencesStore = preferencesStore)
        override val setRepositoryArchivedStateUseCase = SetRepositoryArchivedStateUseCase(preferencesStore = preferencesStore)
        override val setRepositoryGroupUseCase = SetRepositoryGroupUseCase(preferencesStore = preferencesStore)
        override val loadBranchesUseCase = LoadBranchesUseCase(gitClient = gitClient)
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
