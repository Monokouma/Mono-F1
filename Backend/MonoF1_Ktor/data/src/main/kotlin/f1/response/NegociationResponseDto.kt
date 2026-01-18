package f1.response

import f1.entity.NegotiateEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NegociationResponseDto(
    @SerialName("Url") val url: String,
    @SerialName("ConnectionToken") val connectionToken: String,
    @SerialName("ConnectionId") val connectionId: String,
    @SerialName("KeepAliveTimeout") val keepAliveTimeout: Double,
    @SerialName("DisconnectTimeout") val disconnectTimeout: Double,
    @SerialName("ConnectionTimeout") val connectionTimeout: Double,
    @SerialName("TryWebSockets") val tryWebSockets: Boolean,
    @SerialName("ProtocolVersion") val protocolVersion: String,
    @SerialName("TransportConnectTimeout") val transportConnectTimeout: Double,
    @SerialName("LongPollDelay") val longPollDelay: Double
)

fun NegociationResponseDto.toEntity() = NegotiateEntity(
    connectionToken = connectionToken,
    connectionId = connectionId,
    keepAliveTimeout = keepAliveTimeout,
    disconnectTimeout = disconnectTimeout
)