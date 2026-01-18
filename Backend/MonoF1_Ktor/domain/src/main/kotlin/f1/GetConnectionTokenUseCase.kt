package f1

import f1.entity.NegotiateEntity
import f1.request.NegociationRequest

class GetConnectionTokenUseCase(
    private val f1Repository: F1Repository
) {
    suspend operator fun invoke(): Result<NegotiateEntity> = f1Repository.negotiate(
        NegociationRequest(
            connectionData = """[{"name":"streaming"}]""",
            clientProtocol = "1.5"
        )
    )
}