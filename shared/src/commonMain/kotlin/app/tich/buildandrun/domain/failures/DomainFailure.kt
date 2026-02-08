package app.tich.buildandrun.domain.failures

sealed class DomainFailure(
    open val code: DomainFailureCode,
    open val isRetryable: Boolean,
    open val args: List<String> = emptyList(),
    open val details: String? = null,
) {
    data class Validation(
        override val code: DomainFailureCode,
        override val args: List<String>,
        override val details: String? = null,
    ) : DomainFailure(code = code, isRetryable = false, args = args, details = details)

    data class Conflict(
        override val code: DomainFailureCode,
        override val args: List<String>,
        override val isRetryable: Boolean,
        override val details: String? = null,
    ) : DomainFailure(code = code, isRetryable = isRetryable, args = args, details = details)

    data class NotFound(
        override val code: DomainFailureCode,
        override val args: List<String>,
        override val isRetryable: Boolean,
        override val details: String? = null,
    ) : DomainFailure(code = code, isRetryable = isRetryable, args = args, details = details)

    data class PermissionDenied(
        override val code: DomainFailureCode,
        override val args: List<String>,
        override val details: String? = null,
    ) : DomainFailure(code = code, isRetryable = false, args = args, details = details)

    data class ExternalTool(
        override val code: DomainFailureCode,
        override val args: List<String>,
        override val isRetryable: Boolean,
        override val details: String? = null,
    ) : DomainFailure(code = code, isRetryable = isRetryable, args = args, details = details)

    data class Network(
        override val code: DomainFailureCode,
        override val args: List<String>,
        override val isRetryable: Boolean,
        override val details: String? = null,
    ) : DomainFailure(code = code, isRetryable = isRetryable, args = args, details = details)

    data class Unknown(
        override val code: DomainFailureCode,
        override val args: List<String>,
        override val isRetryable: Boolean,
        override val details: String? = null,
    ) : DomainFailure(code = code, isRetryable = isRetryable, args = args, details = details)

    data object Cancelled : DomainFailure(
        code = DomainFailureCode.APP_CANCELLED,
        isRetryable = false,
        args = emptyList(),
    )
}
