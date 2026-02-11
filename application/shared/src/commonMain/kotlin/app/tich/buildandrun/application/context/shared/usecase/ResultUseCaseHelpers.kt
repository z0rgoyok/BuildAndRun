package app.tich.buildandrun.application.context.shared.usecase

fun <T> Result<T>.toUseCaseResult(): UseCaseResult<T> {
    return fold(
        onSuccess = { value -> UseCaseResult.Success(value = value) },
        onFailure = { throwable -> throwable.toUseCaseFailure() },
    )
}
