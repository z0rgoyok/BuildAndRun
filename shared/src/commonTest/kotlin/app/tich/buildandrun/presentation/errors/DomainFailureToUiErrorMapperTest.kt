package app.tich.buildandrun.presentation.errors

import app.tich.buildandrun.domain.failures.DomainFailure
import app.tich.buildandrun.domain.failures.DomainFailureCode
import app.tich.buildandrun.resources.Res
import app.tich.buildandrun.resources.git_worktree_already_exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DomainFailureToUiErrorMapperTest {
    private val mapper = DomainFailureToUiErrorMapper()

    @Test
    fun mapsConflictFailureToConflictUiError() {
        val uiError =
            mapper.map(
                failure =
                    DomainFailure.Conflict(
                        code = DomainFailureCode.GIT_WORKTREE_ALREADY_EXISTS,
                        args = listOf("feature/test"),
                        isRetryable = false,
                    ),
            )

        val resolvedError = requireNotNull(uiError)
        assertEquals(UiErrorKind.Conflict, resolvedError.kind)
        val text = resolvedError.message
        assertEquals(Res.string.git_worktree_already_exists, text.resource)
        assertEquals(listOf("feature/test"), text.args)
    }

    @Test
    fun mapsCancelledToNull() {
        val uiError = mapper.map(failure = DomainFailure.Cancelled)

        assertNull(uiError)
    }
}
