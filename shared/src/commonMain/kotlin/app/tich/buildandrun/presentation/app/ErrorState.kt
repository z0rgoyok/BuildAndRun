package app.tich.buildandrun.presentation.app

import app.tich.buildandrun.domain.shared.failure.DomainFailureCode

data class ErrorState(
    val code: DomainFailureCode,
    val message: String,
    val details: String?,
    val isRetryable: Boolean,
)
