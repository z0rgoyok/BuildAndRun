package app.tich.buildandrun.application.context.repositories.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseResult
import app.tich.buildandrun.domain.context.copy.model.CopyPattern
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId

class LoadPresentationPreferencesUseCase(
    private val preferencesStore: PreferencesStore,
) {
    fun execute(input: Input): UseCaseResult<Output> {
        return runCatchingCancellable {
            val enabledEditorIds =
                input.editorIds
                    .asSequence()
                    .filter { editorId -> preferencesStore.isEditorEnabled(editorId = editorId) }
                    .toSet()
            val repositoryId =
                input.repositoryId
                    ?.trim()
                    ?.takeIf(String::isNotBlank)
                    ?.let(::RepositoryId)
            if (repositoryId == null) {
                return@runCatchingCancellable Output(
                    preferredEditorId = null,
                    enabledEditorIds = enabledEditorIds,
                    selectedRepositoryCustomCopyPatterns = null,
                    selectedRepositoryEffectiveCopyPatterns = input.defaultCopyPatterns.map(CopyPattern::pattern),
                )
            }
            Output(
                preferredEditorId = preferencesStore.preferredEditorId(forRepositoryId = repositoryId),
                enabledEditorIds = enabledEditorIds,
                selectedRepositoryCustomCopyPatterns =
                    preferencesStore.copyPatterns(forRepositoryId = repositoryId)?.map(CopyPattern::pattern),
                selectedRepositoryEffectiveCopyPatterns =
                    preferencesStore.effectiveCopyPatterns(forRepositoryId = repositoryId).map(CopyPattern::pattern),
            )
        }.toUseCaseResult()
    }

    data class Input(
        val repositoryId: String?,
        val editorIds: List<String>,
        val defaultCopyPatterns: List<CopyPattern>,
    )

    data class Output(
        val preferredEditorId: String?,
        val enabledEditorIds: Set<String>,
        val selectedRepositoryCustomCopyPatterns: List<String>?,
        val selectedRepositoryEffectiveCopyPatterns: List<String>,
    )
}
