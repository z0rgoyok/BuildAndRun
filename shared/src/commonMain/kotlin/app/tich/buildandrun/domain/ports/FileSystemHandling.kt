package app.tich.buildandrun.domain.ports

interface FileSystemHandling {
    suspend fun fileExists(atPath: String): Boolean

    suspend fun isDirectory(atPath: String): Boolean

    suspend fun createDirectory(
        atPath: String,
        withIntermediateDirectories: Boolean = true,
    )

    suspend fun copyItem(
        atPath: String,
        toPath: String,
    )

    suspend fun fileSize(atPath: String): Long?

    suspend fun directorySize(atPath: String): Long?

    suspend fun delete(
        atPath: String,
        recursive: Boolean = false,
    )

    suspend fun listDirectory(atPath: String): List<String>

    fun homeDirectory(): String
}
