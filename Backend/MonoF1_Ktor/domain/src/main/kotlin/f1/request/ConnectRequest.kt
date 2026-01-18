package f1.request

data class ConnectRequest(
    val connectionToken: String,
    val subscribeRequest: SubscribeRequest
)

data class SubscribeRequest(
    val topics: List<String> = listOf("TimingData", "SessionInfo", "DriverList")
)