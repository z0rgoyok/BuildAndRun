package app.tich.buildandrun.presentation.app.context.worktrees.impl.usecase

import app.tich.buildandrun.domain.context.copy.model.CopyPattern
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId
import app.tich.buildandrun.presentation.app.core.AppWiring
import app.tich.buildandrun.presentation.app.core.normalizePath

class DefaultCopyConfiguredFilesUseCase(
    private val runtime: AppWiring,
) : CopyConfiguredFilesUseCase {
    override suspend fun execute(
        repositoryPath: String,
        createdWorktreePath: String,
        repositoryId: String,
    ) {
        val normalizedRepositoryPath = normalizePath(repositoryPath)
        val normalizedWorktreePath = normalizePath(createdWorktreePath)
        if (normalizedRepositoryPath.isBlank() || normalizedWorktreePath.isBlank()) {
            return
        }
        val effectivePatterns =
            runtime.graph.preferencesStore.effectiveCopyPatterns(
                forRepositoryId = RepositoryId(repositoryId),
            )
        if (effectivePatterns.isEmpty()) {
            return
        }
        effectivePatterns.forEach { pattern ->
            copyPatternIfExists(
                pattern = pattern,
                sourceRoot = normalizedRepositoryPath,
                destinationRoot = normalizedWorktreePath,
            )
        }
    }

    private suspend fun copyPatternIfExists(
        pattern: CopyPattern,
        sourceRoot: String,
        destinationRoot: String,
    ) {
        val sourcePath = "$sourceRoot/${pattern.pattern}"
        val destinationPath = "$destinationRoot/${pattern.pattern}"
        val sourceExists = runtime.graph.fileSystem.fileExists(atPath = sourcePath)
        if (!sourceExists) {
            return
        }
        runCatching {
            runtime.graph.fileSystem.copyItem(
                atPath = sourcePath,
                toPath = destinationPath,
            )
        }
    }
}
