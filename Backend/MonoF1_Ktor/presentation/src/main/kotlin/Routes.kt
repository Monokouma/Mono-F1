import auth.ValidateApiKeyUseCase
import car.ManageCarUseCase
import car.config.CarsConfig
import car.entity.CarStateEntity
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

    environment?.monitor?.subscribe(ApplicationStarted) {
        CoroutineScope(Dispatchers.IO).launch {
            manageCarUseCase()
                .collect { states ->
                    states?.let { webSocketManager.updateAll(it) }
                }
        }
    }

    get("/api/health") {
        call.respondText("Ow it's working")
    }

    webSocket("/ws/cars") {
        val authorization = call.request.queryParameters["api_key"]
        val carName = call.request.queryParameters["car_name"]

        validateApiKeyUseCase(authorization).onFailure { e ->
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, e.message ?: "Error"))
            return@webSocket
        }

        webSocketManager.register(
            name = carName ?: return@webSocket close(
                reason = CloseReason(
                    code = CloseReason.Codes.VIOLATED_POLICY,
                    message = "car_name required"
                )
            ),
            session = this
        )

        try {
            for (frame in incoming) {
                //Keep alive
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
    }

    get("/api/test/custom/{color}") {
        val status = call.parameters["color"] ?: "FAFAFA"

        val fakeState = CarStateEntity(isOn = true, color = status, brightness = 70)

        webSocketManager.updateAll(
            mapOf(
                "redbull" to fakeState
            )
        )
        
        call.respondText("Simulated: $status")
    }

    get("/api/test/simulate/{status}") {
        val status = call.parameters["status"] ?: "idle"

        val fakeState = when (status.lowercase()) {
            "redbull" -> CarsConfig.CARS.mapValues { (_, teamConfig) ->
                val isLeader = teamConfig.teamName == "Red Bull Racing"
                CarStateEntity(
                    isOn = isLeader,
                    color = if (isLeader) teamConfig.color else CarsConfig.COLOR_OFF,
                    brightness = 70
                )
            }

            "ferrari" -> CarsConfig.CARS.mapValues { (_, teamConfig) ->
                val isLeader = teamConfig.teamName == "Ferrari"
                CarStateEntity(
                    isOn = isLeader,
                    color = if (isLeader) teamConfig.color else CarsConfig.COLOR_OFF,
                    brightness = 70
                )
            }

            "mercedes" -> CarsConfig.CARS.mapValues { (_, teamConfig) ->
                val isLeader = teamConfig.teamName == "Mercedes"
                CarStateEntity(
                    isOn = isLeader,
                    color = if (isLeader) teamConfig.color else CarsConfig.COLOR_OFF,
                    brightness = 70
                )
            }

            "mclaren" -> CarsConfig.CARS.mapValues { (_, teamConfig) ->
                val isLeader = teamConfig.teamName == "McLaren"
                CarStateEntity(
                    isOn = isLeader,
                    color = if (isLeader) teamConfig.color else CarsConfig.COLOR_OFF,
                    brightness = 70
                )
            }

            "off" -> CarsConfig.CARS.mapValues {
                CarStateEntity(isOn = false, color = CarsConfig.COLOR_OFF, brightness = 70)
            }

            else -> CarsConfig.CARS.mapValues {
                CarStateEntity(isOn = true, color = CarsConfig.COLOR_WHITE, brightness = 70)
            }
        }

        webSocketManager.updateAll(fakeState)
        call.respondText("Simulated: $status")
    }
}