package app.tich.buildandrun.presentation.app.context.worktrees.impl

import app.tich.buildandrun.domain.context.copy.model.CopyPattern
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId
import app.tich.buildandrun.presentation.app.core.AppRuntime
import app.tich.buildandrun.presentation.app.core.normalizePath

internal suspend fun AppRuntime.copyConfiguredFiles(
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
        graph.preferencesStore.effectiveCopyPatterns(
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

private suspend fun AppRuntime.copyPatternIfExists(
    pattern: CopyPattern,
    sourceRoot: String,
    destinationRoot: String,
) {
    val sourcePath = "$sourceRoot/${pattern.pattern}"
    val destinationPath = "$destinationRoot/${pattern.pattern}"
    val sourceExists = graph.fileSystem.fileExists(atPath = sourcePath)
    if (!sourceExists) {
        return
    }
    runCatching {
        graph.fileSystem.copyItem(
            atPath = sourcePath,
            toPath = destinationPath,
        )
    }
}
