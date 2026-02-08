package app.tich.buildandrun.domain.failures

sealed class DomainFailure(
    open val code: String,
    open val isRetryable: Boolean,
    open val payload: Map<String, String> = emptyMap(),
) {
    data class Validation(
        override val code: String,
        val reason: String,
        override val payload: Map<String, String>,
    ) : DomainFailure(code = code, isRetryable = false, payload = payload)

    data class Conflict(
        override val code: String,
        override val payload: Map<String, String>,
        override val isRetryable: Boolean,
    ) : DomainFailure(code = code, isRetryable = isRetryable, payload = payload)

    data class NotFound(
        override val code: String,
        override val payload: Map<String, String>,
        override val isRetryable: Boolean,
    ) : DomainFailure(code = code, isRetryable = isRetryable, payload = payload)

    data class PermissionDenied(
        override val code: String,
        override val payload: Map<String, String>,
    ) : DomainFailure(code = code, isRetryable = false, payload = payload)

    data class ExternalTool(
        override val code: String,
        override val payload: Map<String, String>,
        override val isRetryable: Boolean,
    ) : DomainFailure(code = code, isRetryable = isRetryable, payload = payload)

    data class Network(
        override val code: String,
        override val payload: Map<String, String>,
        override val isRetryable: Boolean,
    ) : DomainFailure(code = code, isRetryable = isRetryable, payload = payload)

    data class Unknown(
        override val code: String,
        override val payload: Map<String, String>,
        override val isRetryable: Boolean,
    ) : DomainFailure(code = code, isRetryable = isRetryable, payload = payload)

    data object Cancelled : DomainFailure(
        code = "app.cancelled",
        isRetryable = false,
        payload = emptyMap(),
    )
}
