package app.tich.buildandrun.domain.entities

data class WorktreeStatus(
    val isDirty: Boolean,
    val hasRemote: Boolean,
    val ahead: Int,
    val behind: Int,
    val prStatus: PRStatus?,
) {
    val hasUnpushedCommits: Boolean get() = ahead > 0
    val needsPull: Boolean get() = behind > 0
    val hasPR: Boolean get() = prStatus != null

    companion object {
        val UNKNOWN =
            WorktreeStatus(
                isDirty = false,
                hasRemote = false,
                ahead = 0,
                behind = 0,
                prStatus = null,
            )
    }
}
