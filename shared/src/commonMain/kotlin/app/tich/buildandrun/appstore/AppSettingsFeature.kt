package app.tich.buildandrun.appstore

interface AppSettingsFeature {
    fun onLoadBranches()

    fun branchExists(branch: String): Boolean

    fun onSetWorktreeBasePath(path: String)

    fun preferredBaseBranch(): String?

    fun onSetPreferredBaseBranch(branch: String)

    fun onSetDefaultCopyPatterns(patterns: List<String>)

    fun onSetRepositoryCopyPatterns(patterns: List<String>?)
}
