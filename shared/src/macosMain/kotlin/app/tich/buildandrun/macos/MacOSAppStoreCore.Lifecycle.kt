package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.usecases.UseCaseResult
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal fun MacOSAppStoreCore.loadInitial() {
    scope.launch {
        isLoading = true
        publishState()
        when (val result = graph.loadRepositoriesUseCase.execute()) {
            is UseCaseResult.Success -> {
                repositories = result.value
                selectedRepositoryId = preferredSelectedRepositoryId()
                selectedWorktreePath = null
            }

            is UseCaseResult.Failure -> {
                error = mapFailureToErrorState(result.value)
            }
        }
        repositories.forEach { repository ->
            loadWorktreesForRepositoryInternal(path = repository.path)
        }
        isLoading = false
        publishState()
    }
}

internal fun MacOSAppStoreCore.destroy() {
    scope.cancel()
}
