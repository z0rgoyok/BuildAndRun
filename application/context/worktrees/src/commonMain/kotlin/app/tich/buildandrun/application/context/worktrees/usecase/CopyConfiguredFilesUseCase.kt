package app.tich.buildandrun.application.context.worktrees.usecase

import app.tich.buildandrun.application.context.repositories.port.PreferencesStore
import app.tich.buildandrun.application.context.shared.port.FileSystemHandling
import app.tich.buildandrun.domain.context.copy.model.CopyPattern
import app.tich.buildandrun.domain.context.repositories.model.RepositoryId

class CopyConfiguredFilesUseCase(
    private val preferencesStore: PreferencesStore,
    private val fileSystemHandling: FileSystemHandling,
) {
    suspend fun execute(input: Input) {
        val normalizedRepositoryPath = normalizePath(input.repositoryPath)
        val normalizedWorktreePath = normalizePath(input.createdWorktreePath)
        if (normalizedRepositoryPath.isBlank() || normalizedWorktreePath.isBlank()) {
            return
        }
        val effectivePatterns =
            preferencesStore.effectiveCopyPatterns(
                forRepositoryId = RepositoryId(input.repositoryId),
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
        if (!fileSystemHandling.fileExists(atPath = sourcePath)) {
            return
        }
        runCatching {
            fileSystemHandling.copyItem(
                atPath = sourcePath,
                toPath = destinationPath,
            )
        }
    }

    private fun normalizePath(path: String): String = path.trim().trimEnd('/')

    data class Input(
        val repositoryPath: String,
        val createdWorktreePath: String,
        val repositoryId: String,
    )
}
