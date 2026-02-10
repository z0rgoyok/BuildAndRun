package app.tich.buildandrun.domain.context.copy.model

data class CopyResult(
    val copied: List<String>,
    val skipped: List<String>,
    val failed: List<CopyFailure>,
) {
    val isEmpty: Boolean get() = copied.isEmpty() && skipped.isEmpty() && failed.isEmpty()
    val isSuccess: Boolean get() = failed.isEmpty()

    val summary: String
        get() =
            buildString {
                if (copied.isNotEmpty()) {
                    append("Copied ${copied.size} item(s)")
                }
                if (skipped.isNotEmpty()) {
                    if (isNotEmpty()) append(", ")
                    append("Skipped ${skipped.size} (not found)")
                }
                if (failed.isNotEmpty()) {
                    if (isNotEmpty()) append(", ")
                    append("Failed ${failed.size}")
                }
                if (isEmpty()) append("Nothing to copy")
            }

    companion object {
        val EMPTY =
            CopyResult(
                copied = emptyList(),
                skipped = emptyList(),
                failed = emptyList(),
            )
    }
}
