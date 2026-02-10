package app.tich.buildandrun.appstore

interface AppRepositoriesFeature {
    fun onAddRepositoryPathChanged(value: String)

    fun onAddRepository()

    fun onSelectRepository(repositoryId: String)

    fun onArchiveRepository(repositoryId: String)

    fun onRestoreRepository(repositoryId: String)

    fun onRemoveRepository(repositoryId: String)
}
