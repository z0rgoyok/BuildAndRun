package app.tich.buildandrun.presentation.errors

import app.tich.buildandrun.domain.failures.DomainFailure
import app.tich.buildandrun.presentation.i18n.UiText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DomainFailureToUiErrorMapperTest {
    private val mapper = DomainFailureToUiErrorMapper()

    @Test
    fun mapsConflictFailureToConflictUiError() {
        val uiError =
            mapper.map(
                failure =
                    DomainFailure.Conflict(
                        code = "git.worktree_already_exists",
                        payload = mapOf("name" to "feature/test"),
                        isRetryable = false,
                    ),
            )

        val resolvedError = requireNotNull(uiError)
        assertEquals(UiErrorKind.Conflict, resolvedError.kind)
        val text = resolvedError.message
        assertTrue(text is UiText.Key)
        assertEquals("git.worktree_already_exists", text.key)
        assertEquals(listOf("feature/test"), text.args)
    }

    @Test
    fun mapsCancelledToNull() {
        val uiError = mapper.map(failure = DomainFailure.Cancelled)

        assertNull(uiError)
    }
}
