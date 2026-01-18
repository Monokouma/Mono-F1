import auth.ValidateApiKeyUseCase
import car.ManageCarUseCase
import f1.GetF1DataUseCase
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import manager.WebSocketManager
import org.koin.ktor.ext.inject
import sensor.ProcessSensorDataUseCase


fun Route.getRoutes() {
    val processSensorDataUseCase: ProcessSensorDataUseCase by inject()
    val manageCarUseCase: ManageCarUseCase by inject()
    val webSocketManager: WebSocketManager by inject()
    val validateApiKeyUseCase: ValidateApiKeyUseCase by inject()
    val getF1DataUseCase: GetF1DataUseCase by inject()

    environment?.monitor?.subscribe(ApplicationStarted) {
        CoroutineScope(Dispatchers.IO).launch {
            manageCarUseCase()
                .collect { states ->
                    webSocketManager.updateAll(states)
                }
        }
    }

    get("/api/health") {
        call.respondText("Ow it's working")
    }

    webSocket("/ws/test") {
        try {
            getF1DataUseCase().collect { result ->
                result.onSuccess { f1Entity ->
                    println("Session: ${f1Entity.sessionStatus}")
                    println("Leader: ${f1Entity.leader?.teamName} - ${f1Entity.leader?.driverName}")
                    send(Frame.Text("Leader: ${f1Entity.leader?.teamName}"))
                }.onFailure { error ->
                    println("Error: ${error.message}")
                }
            }
        } catch (e: Exception) {
            println("Connection error: ${e.message}")
        }
    }

    webSocket("/ws/cars") {
        val authorization = call.request.queryParameters["api_key"]
        val carName = call.request.queryParameters["car_name"]

        validateApiKeyUseCase(authorization).onFailure { e ->
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, e.message ?: "Error"))
        }

        webSocketManager.register(
            name = carName ?: return@webSocket close(
                reason = CloseReason(
                    code = CloseReason.Codes.VIOLATED_POLICY,
                    message = "Error"
                )
            ), session = this
        )

        try {
            for (frame in incoming) {
            }
        } finally {
            webSocketManager.unregister(carName)
        }
    }

    webSocket("/ws/sensor") {
        val authorization = call.request.queryParameters["api_key"]

        for (frame in incoming) {
            when (frame) {
                is Frame.Text -> {
                    val json = frame.readText()
                    processSensorDataUseCase.invoke(authorization, json)
                        .onSuccess {
                            send(Frame.Text("OK"))
                        }
                        .onFailure { e ->
                            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, e.message ?: "Error"))
                        }
                }

                else -> {}
            }
        }
        println("WebSocket fermé")

    }

}