package app.tich.buildandrun.presentation.app

import app.tich.buildandrun.application.context.repositories.usecase.*
import app.tich.buildandrun.application.context.shared.port.EditorOpening
import app.tich.buildandrun.application.context.shared.port.FileSystemHandling
import app.tich.buildandrun.application.context.shared.port.SystemOpening
import app.tich.buildandrun.application.context.worktrees.usecase.CreateWorktreeUseCase
import app.tich.buildandrun.application.context.worktrees.usecase.LoadBranchesUseCase
import app.tich.buildandrun.domain.context.editors.model.Editor
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.domain.context.repositories.model.RepositoryGroup
import app.tich.buildandrun.presentation.app.core.AppStoreGraph
import app.tich.buildandrun.testsupport.FakeGitClient
import app.tich.buildandrun.testsupport.FakePreferencesStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class AppStoreSidebarExpansionTest {
    @Test
    fun onToggleVisibleSidebarRepositoriesExpansion_affectsOnlyRepositoriesFromExpandedSections() =
        runBlocking {
            val groupVisible = RepositoryGroup.create(name = "Visible", sortOrder = 0)
            val groupCollapsed = RepositoryGroup.create(name = "Collapsed", sortOrder = 1)
            val ungroupedRepository = Repository.create(path = "/tmp/repo-ungrouped")
            val visibleRepository = Repository.create(path = "/tmp/repo-visible", groupId = groupVisible.id)
            val collapsedRepository = Repository.create(path = "/tmp/repo-collapsed", groupId = groupCollapsed.id)
            val archivedRepository = Repository.create(path = "/tmp/repo-archived", isArchived = true, groupId = groupVisible.id)
            val preferences =
                FakePreferencesStore(
                    initialRepositories =
                        listOf(
                            ungroupedRepository,
                            visibleRepository,
                            collapsedRepository,
                            archivedRepository,
                        ),
                )
            preferences.saveRepositoryGroups(listOf(groupVisible, groupCollapsed))
            preferences.collapsedGroupIds = setOf(groupCollapsed.id.value)
            val store = createAppRootComponent(graph = TestGraph(preferencesStore = preferences))

            waitForRepositories(store = store, expectedCount = 4)

            store.sidebar.onToggleVisibleSidebarRepositoriesExpansion(
                includeArchivedRepositories = false,
                preferredRepositoryId = null,
            )

            assertEquals(
                setOf(ungroupedRepository.id.value, visibleRepository.id.value),
                store.repositoriesState.value.expandedRepositoryIds,
            )
            assertEquals(store.repositoriesState.value.expandedRepositoryIds, preferences.expandedRepositoryIds)

            store.sidebar.onToggleVisibleSidebarRepositoriesExpansion(
                includeArchivedRepositories = false,
                preferredRepositoryId = visibleRepository.id.value,
            )

            assertEquals(setOf(visibleRepository.id.value), store.repositoriesState.value.expandedRepositoryIds)
            assertEquals(store.repositoriesState.value.expandedRepositoryIds, preferences.expandedRepositoryIds)

            store.destroy()
        }

    @Test
    fun sidebarExpansionActions_updateStateAndPreferences() =
        runBlocking {
            val group = RepositoryGroup.create(name = "Team", sortOrder = 0)
            val repository =
                Repository.create(
                    path = "/tmp/repo-one",
                    groupId = group.id,
                )
            val preferences = FakePreferencesStore(initialRepositories = listOf(repository))
            preferences.saveRepositoryGroups(listOf(group))
            val store = createAppRootComponent(graph = TestGraph(preferencesStore = preferences))

            waitForRepositories(store = store, expectedCount = 1)

            store.sidebar.onSetSidebarRepositoryExpanded(repositoryId = repository.id.value, expanded = true)
            store.sidebar.onSetSidebarGroupCollapsed(groupId = group.id.value, collapsed = true)

            assertEquals(setOf(repository.id.value), store.repositoriesState.value.expandedRepositoryIds)
            assertEquals(setOf(group.id.value), store.repositoriesState.value.collapsedGroupIds)
            assertEquals(store.repositoriesState.value.expandedRepositoryIds, preferences.expandedRepositoryIds)
            assertEquals(store.repositoriesState.value.collapsedGroupIds, preferences.collapsedGroupIds)

            store.sidebar.onSyncSidebarSelectionExpansion(repositoryId = repository.id.value)
            assertTrue(store.repositoriesState.value.expandedRepositoryIds.contains(repository.id.value))
            assertEquals(store.repositoriesState.value.expandedRepositoryIds, preferences.expandedRepositoryIds)

            store.destroy()
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
        fail("AppStore did not load repositories in time")
    }

    private class TestGraph(
        override val preferencesStore: FakePreferencesStore,
    ) : AppStoreGraph {
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
