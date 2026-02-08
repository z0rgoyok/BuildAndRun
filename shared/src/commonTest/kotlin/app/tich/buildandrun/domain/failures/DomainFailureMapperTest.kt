package app.tich.buildandrun.domain.failures

import app.tich.buildandrun.domain.errors.AppError
import app.tich.buildandrun.domain.errors.GitError
import kotlin.test.Test

class DomainFailureMapperTest {
    @Test
    fun mapsGitConflictToDomainConflict() {
        val failure = DomainFailureMapper.fromThrowable(GitError.WorktreeAlreadyExists(name = "feature/test"))

        if (failure !is DomainFailure.Conflict) {
            error("Expected DomainFailure.Conflict but was ${failure::class.simpleName}")
        }
        if (failure.code != "git.worktree_already_exists") {
            error("Unexpected failure code: ${failure.code}")
        }
        if (failure.payload["name"] != "feature/test") {
            error("Unexpected payload name: ${failure.payload["name"]}")
        }
    }

    @Test
    fun mapsCancelledToCancelledFailure() {
        val failure = DomainFailureMapper.fromThrowable(AppError.Cancelled)

        if (failure !is DomainFailure.Cancelled) {
            error("Expected DomainFailure.Cancelled but was ${failure::class.simpleName}")
        }
    }
}
