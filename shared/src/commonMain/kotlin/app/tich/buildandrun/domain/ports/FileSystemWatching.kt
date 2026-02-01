package app.tich.buildandrun.domain.ports

interface FileSystemWatching {
    fun setChangeHandler(handler: (changedPaths: Set<String>) -> Unit)

    fun updateWatchedPaths(paths: Set<String>)

    fun stopWatching()
}
