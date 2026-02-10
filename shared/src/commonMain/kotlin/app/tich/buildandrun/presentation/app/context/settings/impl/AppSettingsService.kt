package app.tich.buildandrun.presentation.app.context.settings.impl

import app.tich.buildandrun.presentation.app.AppSettingsFeature
import app.tich.buildandrun.presentation.app.core.AppRuntime

internal class AppSettingsService(
    private val runtime: AppRuntime,
) : AppSettingsFeature {
    override fun onLoadBranches() {
        runtime.onLoadBranches()
    }

    override fun branchExists(branch: String): Boolean = runtime.branchExists(branch = branch)

    override fun onSetWorktreeBasePath(path: String) {
        runtime.onSetWorktreeBasePath(path = path)
    }

    override fun preferredBaseBranch(): String? = runtime.preferredBaseBranch()

    override fun onSetPreferredBaseBranch(branch: String) {
        runtime.onSetPreferredBaseBranch(branch = branch)
    }

    override fun onSetDefaultCopyPatterns(patterns: List<String>) {
        runtime.onSetDefaultCopyPatterns(patterns = patterns)
    }

    override fun onSetRepositoryCopyPatterns(patterns: List<String>?) {
        runtime.onSetRepositoryCopyPatterns(patterns = patterns)
    }
}
