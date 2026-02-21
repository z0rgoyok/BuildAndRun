package app.tich.buildandrun.application.usecases

import app.tich.buildandrun.application.context.repositories.usecase.LoadPresentationPreferencesUseCase
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.copy.model.CopyPattern
import app.tich.buildandrun.domain.context.repositories.model.Repository
import app.tich.buildandrun.testsupport.FakePreferencesStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LoadPresentationPreferencesUseCaseTest {
    @Test
    fun execute_withoutSelectedRepository_usesDefaultPatternsAndNoPreferredEditor() {
        val store = FakePreferencesStore()
        store.enabledEditorIds = setOf("vscode")
        val useCase = LoadPresentationPreferencesUseCase(preferencesStore = store)

        val result =
            useCase.execute(
                input =
                    LoadPresentationPreferencesUseCase.Input(
                        repositoryId = null,
                        editorIds = listOf("idea", "vscode"),
                        defaultCopyPatterns = listOf(CopyPattern(pattern = "*.env"), CopyPattern(pattern = "config/*.json")),
                    ),
            )

        when (result) {
            is UseCaseResult.Success -> {
                assertNull(result.value.preferredEditorId)
                assertEquals(setOf("vscode"), result.value.enabledEditorIds)
                assertNull(result.value.selectedRepositoryCustomCopyPatterns)
                assertEquals(listOf("*.env", "config/*.json"), result.value.selectedRepositoryEffectiveCopyPatterns)
            }

            is UseCaseResult.Failure -> error("Expected success")
        }
    }

    @Test
    fun execute_withSelectedRepository_returnsRepositorySpecificProjection() {
        val repository = Repository.create(path = "/tmp/repo-a")
        val store = FakePreferencesStore(initialRepositories = listOf(repository))
        store.enabledEditorIds = setOf("idea")
        store.defaultCopyPatterns = listOf(CopyPattern(pattern = "*.default"))
        store.setPreferredEditorId(editorId = "idea", forRepositoryId = repository.id)
        store.setCopyPatterns(
            patterns = listOf(CopyPattern(pattern = "*.repo")),
            forRepositoryId = repository.id,
        )
        val useCase = LoadPresentationPreferencesUseCase(preferencesStore = store)

        val result =
            useCase.execute(
                input =
                    LoadPresentationPreferencesUseCase.Input(
                        repositoryId = repository.id.value,
                        editorIds = listOf("idea", "vscode"),
                        defaultCopyPatterns = store.defaultCopyPatterns,
                    ),
            )

        when (result) {
            is UseCaseResult.Success -> {
                assertEquals("idea", result.value.preferredEditorId)
                assertEquals(setOf("idea"), result.value.enabledEditorIds)
                assertEquals(listOf("*.repo"), result.value.selectedRepositoryCustomCopyPatterns)
                assertEquals(listOf("*.repo"), result.value.selectedRepositoryEffectiveCopyPatterns)
            }

            is UseCaseResult.Failure -> error("Expected success")
        }
    }
}
