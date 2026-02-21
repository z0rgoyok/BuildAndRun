package app.tich.buildandrun.presentation.app

interface AppTextsFeature {
    fun resolveBranchConflictMessage(branch: String): String

    fun resolveBranchConflictUseExistingDetail(branch: String): String

    fun resolveStatusToPush(commits: String): String

    fun resolveStatusBehind(commits: String): String

    fun resolveNewTaskIn(column: String): String

    fun resolveCompleteWorktreeCleanup(name: String): String

    fun resolveCompleteWorktreeUnpushedCommits(commits: String): String

    fun resolveCompleteWorktreeUpdateTargetFromRemoteFirst(branch: String): String
}
