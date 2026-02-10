package app.tich.buildandrun.macos

import app.tich.buildandrun.application.usecases.AddRepositoryUseCase
import app.tich.buildandrun.application.usecases.LoadRepositoriesUseCase
import app.tich.buildandrun.application.usecases.UseCaseResult
import app.tich.buildandrun.domain.entities.*
import app.tich.buildandrun.testsupport.FakeGitClient
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDefaults
import kotlin.test.*

class MacOSPreferencesStorePersistenceTest {
    @Test
    fun persistsRepositoriesAcrossStoreInstances() =
        runBlocking {
            withIsolatedDefaults { defaults ->
                val repository =
                    Repository(
                        id = RepositoryId(value = "repo-1"),
                        path = "/tmp/repo-1",
                        name = "repo-1",
                        isArchived = false,
                    )
                val writer = MacOSPreferencesStore(defaults = defaults)
                writer.saveRepositories(repositories = listOf(repository))

                val reader = MacOSPreferencesStore(defaults = defaults)
                assertEquals(listOf(repository), reader.loadRepositories())
            }
        }

    @Test
    fun persistsAllPreferencesAcrossStoreInstances() =
        runBlocking {
            withIsolatedDefaults { defaults ->
                val writer = MacOSPreferencesStore(defaults = defaults)
                val repositoryId = RepositoryId(value = "repo-42")
                val worktreePath = "/tmp/repo-42/worktree-feature"

                writer.worktreeBasePath = "/tmp/worktrees"
                writer.expandedRepositoryIds = setOf("repo-42", "repo-43")
                writer.lastSelectedRepositoryId = "repo-42"
                writer.lastSelectedWorktreePath = worktreePath
                writer.rememberEditorChoice = false
                writer.setPreferredEditorId(
                    editorId = "xcode",
                    forRepositoryId = repositoryId,
                )
                writer.enabledEditorIds = setOf("xcode", "cursor")
                writer.setPreferredBaseBranch(
                    branch = "develop",
                    forRepositoryId = repositoryId,
                )
                writer.setWorktreeBaseBranch(
                    branch = "main",
                    forWorktreePath = worktreePath,
                )
                writer.defaultCopyPatterns = listOf(CopyPattern(pattern = ".env"), CopyPattern(pattern = ".tool-versions"))
                writer.setCopyPatterns(
                    patterns = listOf(CopyPattern(pattern = ".npmrc")),
                    forRepositoryId = repositoryId,
                )
                writer.setKanbanTasks(
                    tasks =
                        listOf(
                            KanbanTask(
                                id = KanbanTaskId(value = "task-1"),
                                title = "Persist kanban",
                                description = "Task must survive restart",
                                columnId = KanbanColumnType.IN_PROGRESS,
                                worktreePath = null,
                                createdAt = 101L,
                                order = 3,
                            ),
                        ),
                    forRepositoryId = repositoryId,
                )

                val reader = MacOSPreferencesStore(defaults = defaults)
                assertEquals("/tmp/worktrees", reader.worktreeBasePath)
                assertEquals(setOf("repo-42", "repo-43"), reader.expandedRepositoryIds)
                assertEquals("repo-42", reader.lastSelectedRepositoryId)
                assertEquals(worktreePath, reader.lastSelectedWorktreePath)
                assertFalse(reader.rememberEditorChoice)
                assertEquals("xcode", reader.preferredEditorId(forRepositoryId = repositoryId))
                assertEquals(setOf("xcode", "cursor"), reader.enabledEditorIds)
                assertEquals("develop", reader.preferredBaseBranch(forRepositoryId = repositoryId))
                assertEquals("main", reader.worktreeBaseBranch(forWorktreePath = worktreePath))
                assertEquals(listOf(".env", ".tool-versions"), reader.defaultCopyPatterns.map(CopyPattern::pattern))
                assertEquals(
                    listOf(".npmrc"),
                    reader.copyPatterns(forRepositoryId = repositoryId)?.map(CopyPattern::pattern),
                )
                assertEquals(
                    listOf(".npmrc"),
                    reader.effectiveCopyPatterns(forRepositoryId = repositoryId).map(CopyPattern::pattern),
                )
                assertEquals(
                    listOf("Persist kanban"),
                    reader.loadKanbanTasks(forRepositoryId = repositoryId).map(KanbanTask::title),
                )
            }
        }

    @Test
    fun addRepositoryUseCaseDataAvailableAfterRestart() =
        runBlocking {
            withIsolatedDefaults { defaults ->
                val repositoryPath = "/tmp/repository-a"
                val gitClient =
                    FakeGitClient().apply {
                        registerRepository(path = repositoryPath)
                        setWorktrees(
                            repositoryPath = repositoryPath,
                            worktrees =
                                listOf(
                                    Worktree(
                                        path = repositoryPath,
                                        branch = "main",
                                        isMain = true,
                                        commitHash = null,
                                        isLocked = false,
                                        isPrunable = false,
                                        baseBranch = null,
                                    ),
                                ),
                        )
                    }

                val firstStore = MacOSPreferencesStore(defaults = defaults)
                val addUseCase =
                    AddRepositoryUseCase(
                        gitClient = gitClient,
                        preferencesStore = firstStore,
                    )
                val addResult = addUseCase.execute(input = AddRepositoryUseCase.Input(path = repositoryPath))
                assertTrue(addResult is UseCaseResult.Success)

                val secondStore = MacOSPreferencesStore(defaults = defaults)
                val loadUseCase = LoadRepositoriesUseCase(preferencesStore = secondStore)
                val loadResult = loadUseCase.execute()
                assertTrue(loadResult is UseCaseResult.Success)
                assertEquals(1, loadResult.value.size)
                assertEquals(repositoryPath, loadResult.value.single().path)
            }
        }

    @Test
    fun supportsNullAndRemovalValuesAcrossRestart() =
        runBlocking {
            withIsolatedDefaults { defaults ->
                val repositoryId = RepositoryId(value = "repo-z")
                val worktreePath = "/tmp/repo-z/wt"
                val writer = MacOSPreferencesStore(defaults = defaults)

                writer.lastSelectedRepositoryId = "repo-z"
                writer.lastSelectedWorktreePath = worktreePath
                writer.enabledEditorIds = setOf("xcode")
                writer.setPreferredEditorId(editorId = "xcode", forRepositoryId = repositoryId)
                writer.setWorktreeBaseBranch(branch = "main", forWorktreePath = worktreePath)
                writer.setCopyPatterns(patterns = listOf(CopyPattern(pattern = ".env")), forRepositoryId = repositoryId)

                writer.lastSelectedRepositoryId = null
                writer.lastSelectedWorktreePath = null
                writer.enabledEditorIds = null
                writer.removePreferredEditorId(forRepositoryId = repositoryId)
                writer.removeWorktreeBaseBranch(forWorktreePath = worktreePath)
                writer.removeCopyPatterns(forRepositoryId = repositoryId)
                writer.setKanbanTasks(
                    tasks =
                        listOf(
                            KanbanTask(
                                id = KanbanTaskId(value = "task-temp"),
                                title = "Temp",
                                description = null,
                                columnId = KanbanColumnType.TODO,
                                worktreePath = null,
                                createdAt = 11L,
                                order = 1,
                            ),
                        ),
                    forRepositoryId = repositoryId,
                )
                writer.removeKanbanTasks(forRepositoryId = repositoryId)

                val reader = MacOSPreferencesStore(defaults = defaults)
                assertNull(reader.lastSelectedRepositoryId)
                assertNull(reader.lastSelectedWorktreePath)
                assertNull(reader.enabledEditorIds)
                assertNull(reader.preferredEditorId(forRepositoryId = repositoryId))
                assertNull(reader.worktreeBaseBranch(forWorktreePath = worktreePath))
                assertNull(reader.copyPatterns(forRepositoryId = repositoryId))
                assertEquals(emptyList(), reader.loadKanbanTasks(forRepositoryId = repositoryId))
            }
        }

    @Test
    fun usesExpectedDefaultsWhenValueNotSet() =
        runBlocking {
            withIsolatedDefaults { defaults ->
                val store = MacOSPreferencesStore(defaults = defaults)
                assertTrue(store.rememberEditorChoice)
                assertEquals("", store.worktreeBasePath)
                assertEquals(emptySet(), store.expandedRepositoryIds)
                assertEquals(emptyList(), store.defaultCopyPatterns)
                assertNull(store.enabledEditorIds)
                assertNull(store.lastSelectedRepositoryId)
                assertNull(store.lastSelectedWorktreePath)
                assertEquals(emptyList(), store.loadKanbanTasks(forRepositoryId = RepositoryId(value = "repo-default")))
            }
        }

    @Test
    fun ignoresMalformedStructuredPreferencesValues() =
        runBlocking {
            withIsolatedDefaults { defaults ->
                defaults.setObject(value = listOf(1, 2), forKey = "preferences.expandedRepositoryIds")
                defaults.setObject(value = mapOf("invalid" to 1), forKey = "preferences.enabledEditorIds")
                defaults.setObject(value = listOf(1, 2), forKey = "preferences.defaultCopyPatterns")
                defaults.setObject(value = listOf("invalid"), forKey = "preferences.preferredEditorIds")
                defaults.setObject(value = listOf("invalid"), forKey = "preferences.preferredBaseBranches")
                defaults.setObject(value = listOf("invalid"), forKey = "preferences.worktreeBaseBranches")
                defaults.setObject(value = mapOf("repo-1" to listOf(1, 2)), forKey = "preferences.repositoryCopyPatterns")
                defaults.setObject(value = mapOf("repo-1" to listOf(1, 2)), forKey = "preferences.repositoryKanbanTasks")

                val store = MacOSPreferencesStore(defaults = defaults)
                val repositoryId = RepositoryId(value = "repo-1")
                assertEquals(emptySet(), store.expandedRepositoryIds)
                assertNull(store.enabledEditorIds)
                assertEquals(emptyList(), store.defaultCopyPatterns)
                assertNull(store.preferredEditorId(forRepositoryId = repositoryId))
                assertNull(store.preferredBaseBranch(forRepositoryId = repositoryId))
                assertNull(store.worktreeBaseBranch(forWorktreePath = "/tmp/repo-1/wt"))
                assertNull(store.copyPatterns(forRepositoryId = repositoryId))
                assertEquals(emptyList(), store.effectiveCopyPatterns(forRepositoryId = repositoryId))
                assertEquals(emptyList(), store.loadKanbanTasks(forRepositoryId = repositoryId))
            }
        }

    private suspend fun withIsolatedDefaults(block: suspend (NSUserDefaults) -> Unit) {
        val suiteName = "app.tich.buildandrun.tests.${NSUUID().UUIDString()}"
        val defaults = NSUserDefaults(suiteName = suiteName)
        assertNotNull(defaults)
        defaults.removePersistentDomainForName(domainName = suiteName)
        try {
            block(defaults)
        } finally {
            defaults.removePersistentDomainForName(domainName = suiteName)
            defaults.synchronize()
        }
    }
}
