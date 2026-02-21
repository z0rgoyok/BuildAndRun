package app.tich.buildandrun.presentation.app.context.texts.impl

import app.tich.buildandrun.presentation.app.AppTextsFeature

class AppTextsService : AppTextsFeature {
    override fun resolveBranchConflictMessage(branch: String): String = resolveBranchConflictMessageText(branch = branch)

    override fun resolveBranchConflictUseExistingDetail(branch: String): String =
        resolveBranchConflictUseExistingDetailText(branch = branch)

    override fun resolveStatusToPush(commits: String): String = resolveStatusToPushText(commits = commits)

    override fun resolveStatusBehind(commits: String): String = resolveStatusBehindText(commits = commits)

    override fun resolveNewTaskIn(column: String): String = resolveNewTaskInText(column = column)

    override fun resolveCompleteWorktreeCleanup(name: String): String = resolveCompleteWorktreeCleanupText(name = name)

    override fun resolveCompleteWorktreeUnpushedCommits(commits: String): String =
        resolveCompleteWorktreeUnpushedCommitsText(commits = commits)

    override fun resolveCompleteWorktreeUpdateTargetFromRemoteFirst(branch: String): String =
        resolveCompleteWorktreeUpdateTargetFromRemoteFirstText(branch = branch)
}
