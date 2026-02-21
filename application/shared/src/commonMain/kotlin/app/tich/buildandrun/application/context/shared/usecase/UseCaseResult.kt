package app.tich.buildandrun.application.context.shared.usecase

import app.tich.buildandrun.domain.shared.failure.DomainFailure

sealed class UseCaseResult<out T> {
    data class Success<T>(val value: T) : UseCaseResult<T>()

    data class Failure(val value: DomainFailure) : UseCaseResult<Nothing>()
}
