package app.tich.buildandrun.macos

import app.tich.buildandrun.application.ports.FileSystemHandling
import platform.Foundation.NSHomeDirectory

class MacOSFileSystemHandling : FileSystemHandling {
    override suspend fun fileExists(atPath: String): Boolean {
        val path = atPath.trim()
        if (path.isBlank()) {
            return false
        }
        val (exitCode, _) = runShellCommand(arguments = listOf("test", "-e", path))
        return exitCode == 0
    }

    override suspend fun isDirectory(atPath: String): Boolean {
        val path = atPath.trim()
        if (path.isBlank()) {
            return false
        }
        val (exitCode, _) = runShellCommand(arguments = listOf("test", "-d", path))
        return exitCode == 0
    }

    override suspend fun createDirectory(
        atPath: String,
        withIntermediateDirectories: Boolean,
    ) {
        val path = atPath.trim()
        if (path.isBlank()) {
            return
        }
        val arguments =
            if (withIntermediateDirectories) {
                listOf("mkdir", "-p", path)
            } else {
                listOf("mkdir", path)
            }
        val (exitCode, output) = runShellCommand(arguments = arguments)
        if (exitCode != 0) {
            error(output.ifBlank { "failed_to_create_directory" })
        }
    }

    override suspend fun copyItem(
        atPath: String,
        toPath: String,
    ) {
        val sourcePath = atPath.trim()
        val destinationPath = toPath.trim()
        if (sourcePath.isBlank() || destinationPath.isBlank()) {
            return
        }
        val parentPath =
            destinationPath.substringBeforeLast(
                delimiter = "/",
                missingDelimiterValue = "",
            )
        if (parentPath.isNotBlank()) {
            createDirectory(
                atPath = parentPath,
                withIntermediateDirectories = true,
            )
        }
        val (exitCode, output) = runShellCommand(arguments = listOf("cp", "-R", sourcePath, destinationPath))
        if (exitCode != 0) {
            error(output.ifBlank { "failed_to_copy_item" })
        }
    }

    override suspend fun fileSize(atPath: String): Long? {
        val path = atPath.trim()
        if (path.isBlank()) {
            return null
        }
        val (exitCode, output) = runShellCommand(arguments = listOf("stat", "-f", "%z", path))
        if (exitCode != 0) {
            return null
        }
        return output.trim().toLongOrNull()
    }

    override suspend fun directorySize(atPath: String): Long? {
        val path = atPath.trim()
        if (path.isBlank()) {
            return null
        }
        val (exitCode, output) = runShellCommand(arguments = listOf("du", "-sk", path))
        if (exitCode != 0) {
            return null
        }
        val sizeKb =
            output
                .trim()
                .substringBefore('\t')
                .substringBefore(' ')
                .toLongOrNull()
                ?: return null
        return sizeKb * 1024
    }

    override suspend fun delete(
        atPath: String,
        recursive: Boolean,
    ) {
        val path = atPath.trim()
        if (path.isBlank()) {
            return
        }
        val arguments =
            if (recursive) {
                listOf("rm", "-rf", path)
            } else {
                listOf("rm", "-f", path)
            }
        val (exitCode, output) = runShellCommand(arguments = arguments)
        if (exitCode != 0) {
            error(output.ifBlank { "failed_to_delete" })
        }
    }

    override suspend fun listDirectory(atPath: String): List<String> {
        val path = atPath.trim()
        if (path.isBlank()) {
            return emptyList()
        }
        val (exitCode, output) = runShellCommand(arguments = listOf("ls", "-A1", path))
        if (exitCode != 0 || output.isBlank()) {
            return emptyList()
        }
        return output
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toList()
    }

    override fun homeDirectory(): String = NSHomeDirectory()
}
