package app.tich.buildandrun.presentation.app.context.repositories.impl

import app.tich.buildandrun.presentation.app.AppRepositoriesFeature
import app.tich.buildandrun.presentation.app.core.AppRuntime

internal class AppRepositoriesService(
    private val runtime: AppRuntime,
) : AppRepositoriesFeature {
    override fun onAddRepositoryPathChanged(value: String) {
        runtime.onAddRepositoryPathChanged(value = value)
    }

    override fun onAddRepository() {
        runtime.onAddRepository()
    }

    override fun onSelectRepository(repositoryId: String) {
        runtime.onSelectRepository(repositoryId = repositoryId)
    }

    override fun onArchiveRepository(repositoryId: String) {
        runtime.onArchiveRepository(repositoryId = repositoryId)
    }

    override fun onRestoreRepository(repositoryId: String) {
        runtime.onRestoreRepository(repositoryId = repositoryId)
    }

    override fun onRemoveRepository(repositoryId: String) {
        runtime.onRemoveRepository(repositoryId = repositoryId)
    }
}
