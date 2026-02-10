package app.tich.buildandrun.presentation.app

import app.tich.buildandrun.application.context.repositories.usecase.*
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.port.FileSystemHandling
import app.tich.buildandrun.application.context.shared.port.SystemOpening
import app.tich.buildandrun.application.context.worktrees.usecase.CreateWorktreeUseCase
import app.tich.buildandrun.application.context.worktrees.usecase.LoadBranchesUseCase
import app.tich.buildandrun.domain.context.editors.model.Editor
import app.tich.buildandrun.domain.context.kanban.model.KanbanColumnType
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode
import app.tich.buildandrun.presentation.app.core.AppGraph
import app.tich.buildandrun.presentation.i18n.UiTextLocalizer
import app.tich.buildandrun.testsupport.FakeGitClient
import app.tich.buildandrun.testsupport.FakePreferencesStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class AppStoreKanbanTaskUpdateTest {
    @Test
    fun onUpdateTask_updatesTitleAndDescription() =
        runBlocking {
            withTestLocalizer {
                val repository = Repository.create(path = "/tmp/repo-one")
                val preferences = FakePreferencesStore(initialRepositories = listOf(repository))
                val graph = TestGraph(preferencesStore = preferences)
                val store = createAppRootComponent(graph = graph)

                waitForRepositories(store = store, expectedCount = 1)
                store.repositories.onSelectRepository(repositoryId = repository.id.value)
                store.kanban.onAddTask(
                    title = "Initial title",
                    description = "Initial description",
                    column = KanbanColumnType.TODO,
                )
                val taskBefore = assertNotNull(store.kanbanState.value.kanbanTasks.firstOrNull())

                store.kanban.onUpdateTask(
                    taskId = taskBefore.id,
                    title = "Updated title",
                    description = "Updated **markdown** text",
                )

                val taskAfter = assertNotNull(store.kanbanState.value.kanbanTasks.firstOrNull())
                assertEquals("Updated title", taskAfter.title)
                assertEquals("Updated **markdown** text", taskAfter.description)
                assertNull(store.messagesState.value.error)

                store.destroy()
            }
        }

    @Test
    fun onUpdateTask_withBlankTitle_setsValidationError() =
        runBlocking {
            withTestLocalizer {
                val repository = Repository.create(path = "/tmp/repo-two")
                val preferences = FakePreferencesStore(initialRepositories = listOf(repository))
                val graph = TestGraph(preferencesStore = preferences)
                val store = createAppRootComponent(graph = graph)

                waitForRepositories(store = store, expectedCount = 1)
                store.repositories.onSelectRepository(repositoryId = repository.id.value)
                store.kanban.onAddTask(
                    title = "Task title",
                    description = "Task description",
                    column = KanbanColumnType.TODO,
                )
                val originalTask = assertNotNull(store.kanbanState.value.kanbanTasks.firstOrNull())

                store.kanban.onUpdateTask(
                    taskId = originalTask.id,
                    title = "  ",
                    description = "Should not apply",
                )

                val state = store.kanbanState.value
                val unchangedTask = assertNotNull(state.kanbanTasks.firstOrNull())
                assertEquals("Task title", unchangedTask.title)
                assertEquals("Task description", unchangedTask.description)
                assertEquals(DomainFailureCode.APP_VALIDATION_TASK_TITLE_BLANK, store.messagesState.value.error?.code)

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

    private class TestGraph(
        override val preferencesStore: FakePreferencesStore,
    ) : AppGraph {
        override val gitClient = FakeGitClient()
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
