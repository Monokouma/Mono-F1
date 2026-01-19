package f1

import f1.entity.F1Entity
import f1.request.ConnectRequest
import f1.request.SubscribeRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetF1DataUseCase(
    private val f1Repository: F1Repository,
    private val getConnectionTokenUseCase: GetConnectionTokenUseCase
) {
    operator fun invoke(): Flow<Result<F1Entity>> = flow {
        println("F1: Starting negotiate...")
        val connectionNegociation = getConnectionTokenUseCase().getOrNull()
            ?: return@flow emit(Result.failure(Exception("Negotiate connection token")))
        println("F1: Negotiate done: $connectionNegociation")

        f1Repository.connect(
            ConnectRequest(
                connectionNegociation.connectionToken,
                SubscribeRequest()
            )
        ).collect { result ->
            val response = result.getOrNull() ?: return@collect

            emit(Result.success(response))
        }
    }
}