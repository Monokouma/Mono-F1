package f1

import f1.entity.F1Entity
import f1.entity.NegotiateEntity
import f1.request.ConnectRequest
import f1.request.NegociationRequest
import kotlinx.coroutines.flow.Flow

interface F1Repository {
    suspend fun negotiate(negociationRequest: NegociationRequest): Result<NegotiateEntity>
    fun connect(connectRequest: ConnectRequest): Flow<Result<F1Entity>>
}