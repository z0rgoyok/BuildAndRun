package app.tich.buildandrun.appstore

import app.tich.buildandrun.domain.entities.CopyPattern
import app.tich.buildandrun.domain.entities.RepositoryId

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
