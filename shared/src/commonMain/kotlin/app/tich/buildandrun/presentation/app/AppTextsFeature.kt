package app.tich.buildandrun.presentation.app

interface AppTextsFeature {
    fun resolveBranchConflictMessage(branch: String): String

    fun resolveBranchConflictUseExistingDetail(branch: String): String

    fun resolveStatusToPush(commits: String): String

    fun resolveStatusBehind(commits: String): String

    fun resolveNewTaskIn(column: String): String
}
