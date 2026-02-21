package app.tich.buildandrun.application.context.worktrees.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.path.normalizePath
import app.tich.buildandrun.application.context.shared.port.FileSystemHandling
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.application.context.shared.usecase.runCatchingCancellable
import app.tich.buildandrun.application.context.shared.usecase.toUseCaseFailure
import app.tich.buildandrun.domain.context.copy.model.CopyPattern
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId
import app.tich.buildandrun.domain.shared.failure.DomainFailure
import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

class CopyConfiguredFilesUseCase(
    private val preferencesStore: PreferencesStore,
    private val fileSystemHandling: FileSystemHandling,
) {
    suspend fun execute(input: Input): UseCaseResult<Output> {
        val repositoryId = input.repositoryId.trim()
        if (repositoryId.isBlank()) {
            return repositoryIdBlankFailure()
        }
        val normalizedRepositoryPath = normalizePath(input.repositoryPath)
        val normalizedWorktreePath = normalizePath(input.createdWorktreePath)
        if (normalizedRepositoryPath.isBlank()) {
            return repositoryPathBlankFailure()
        }
        if (normalizedWorktreePath.isBlank()) {
            return worktreePathBlankFailure()
        }
        return runCatchingCancellable {
            val effectivePatterns =
                preferencesStore.effectiveCopyPatterns(
                    forRepositoryId = RepositoryId(repositoryId),
                )
            if (effectivePatterns.isEmpty()) {
                return@runCatchingCancellable UseCaseResult.Success(value = Output(copiedPatterns = emptyList()))
            }
            val copiedPatterns = mutableListOf<String>()
            effectivePatterns.forEach { pattern ->
                if (
                    copyPatternIfExists(
                        pattern = pattern,
                        sourceRoot = normalizedRepositoryPath,
                        destinationRoot = normalizedWorktreePath,
                    )
                ) {
                    copiedPatterns += pattern.pattern
                }
            }
            UseCaseResult.Success(value = Output(copiedPatterns = copiedPatterns))
        }.fold(
            onSuccess = { it },
            onFailure = { throwable -> throwable.toUseCaseFailure() },
        )
    }

    private suspend fun copyPatternIfExists(
        pattern: CopyPattern,
        sourceRoot: String,
        destinationRoot: String,
    ): Boolean {
        val sourcePath = "$sourceRoot/${pattern.pattern}"
        val destinationPath = "$destinationRoot/${pattern.pattern}"
        if (!fileSystemHandling.fileExists(atPath = sourcePath)) {
            return false
        }
        fileSystemHandling.copyItem(
            atPath = sourcePath,
            toPath = destinationPath,
        )
        return true
    }

    data class Input(
        val repositoryPath: String,
        val createdWorktreePath: String,
        val repositoryId: String,
    )

    data class Output(
        val copiedPatterns: List<String>,
    )

    private fun repositoryIdBlankFailure(): UseCaseResult.Failure =
        UseCaseResult.Failure(
            value =
                DomainFailure.Validation(
                    code = DomainFailureCode.APP_VALIDATION_REPOSITORY_ID_BLANK,
                    args = emptyList(),
                ),
        )

    private fun repositoryPathBlankFailure(): UseCaseResult.Failure =
        UseCaseResult.Failure(
            value =
                DomainFailure.Validation(
                    code = DomainFailureCode.APP_VALIDATION_REPOSITORY_PATH_BLANK,
                    args = emptyList(),
                ),
        )

    private fun worktreePathBlankFailure(): UseCaseResult.Failure =
        UseCaseResult.Failure(
            value =
                DomainFailure.Validation(
                    code = DomainFailureCode.APP_VALIDATION_WORKTREE_PATH_BLANK,
                    args = emptyList(),
                ),
        )
}
