package app.tich.buildandrun.application.context.shared.usecase

import app.tich.buildandrun.domain.shared.failure.DomainFailureMapper

fun Throwable.toUseCaseFailure(): UseCaseResult.Failure {
    return UseCaseResult.Failure(value = DomainFailureMapper.fromThrowable(this))
}
