import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.koin.ktor.ext.inject
import sensor.ProcessSensorDataUseCase

fun Route.getRoutes() {
    val processSensorDataUseCase: ProcessSensorDataUseCase by inject()

    get("/api/health") {
        call.respondText("Ow it's working")
    }

    webSocket("/ws/sensor") {
        println("WebSocket connecté")
        val authorization = call.request.queryParameters["api_key"]
        println("Auth: $authorization")

        for (frame in incoming) {
            when (frame) {
                is Frame.Text -> {
                    val json = frame.readText()
                    println("JSON: $json")

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