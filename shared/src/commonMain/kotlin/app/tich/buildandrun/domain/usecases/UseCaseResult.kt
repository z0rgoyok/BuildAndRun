package app.tich.buildandrun.domain.usecases

import app.tich.buildandrun.domain.failures.DomainFailure

sealed class UseCaseResult<out T> {
    data class Success<T>(val value: T) : UseCaseResult<T>()

    data class Failure(val value: DomainFailure) : UseCaseResult<Nothing>()
}
