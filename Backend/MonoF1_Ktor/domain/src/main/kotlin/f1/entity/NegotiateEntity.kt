package f1.entity

data class NegotiateEntity(
    val connectionToken: String,
    val connectionId: String,
    val keepAliveTimeout: Double,
    val disconnectTimeout: Double
)