package app.tich.buildandrun.domain.shared.failure

import app.tich.buildandrun.domain.shared.error.AppError
import app.tich.buildandrun.domain.shared.error.GitError
import kotlin.test.Test

class DomainFailureMapperTest {
    @Test
    fun mapsGitConflictToDomainConflict() {
        val failure = DomainFailureMapper.fromThrowable(GitError.WorktreeAlreadyExists(name = "feature/test"))

        if (failure !is DomainFailure.Conflict) {
            error("Expected DomainFailure.Conflict but was ${failure::class.simpleName}")
        }
        if (failure.code != DomainFailureCode.GIT_WORKTREE_ALREADY_EXISTS) {
            error("Unexpected failure code: ${failure.code}")
        }
        if (failure.args != listOf("feature/test")) {
            error("Unexpected args: ${failure.args}")
        }
    }

    @Test
    fun mapsCancelledToCancelledFailure() {
        val failure = DomainFailureMapper.fromThrowable(AppError.Cancelled())

        if (failure !is DomainFailure.Cancelled) {
            error("Expected DomainFailure.Cancelled but was ${failure::class.simpleName}")
        }
    }
}
