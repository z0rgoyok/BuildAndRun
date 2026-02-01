package app.tich.buildandrun.domain.entities

data class CopyPreviewItem(
    val pattern: String,
    val exists: Boolean,
    val sizeBytes: Long?,
    val isDirectory: Boolean,
) {
    val id: String get() = pattern

    val formattedSize: String?
        get() = sizeBytes?.let { formatBytes(it) }

    companion object {
        private fun formatBytes(bytes: Long): String =
            when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
                else -> "${bytes / (1024 * 1024 * 1024)} GB"
            }
    }
}
