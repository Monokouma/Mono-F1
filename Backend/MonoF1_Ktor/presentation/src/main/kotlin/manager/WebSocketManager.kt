package manager

import car.entity.CarStateEntity
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class WebSocketManager {

    private val connections = ConcurrentHashMap<String, WebSocketSession>()
    private val carStates = ConcurrentHashMap<String, CarStateEntity>()

    fun register(name: String, session: WebSocketSession) {
        connections[name] = session
        println("🚗 Car registered: $name")
    }

    fun unregister(name: String) {
        connections.remove(name)
        carStates.remove(name)
        println("🚗 Car unregistered: $name")
    }

    fun isConnected(name: String): Boolean = connections.containsKey(name)

    fun getConnectedCars(): Set<String> = connections.keys.toSet()

    suspend fun updateCar(name: String, newState: CarStateEntity) {
        val currentState = carStates[name]

        if (currentState != newState) {
            carStates[name] = newState

            val json = Json.encodeToString(newState)
            connections[name]?.let { session ->
                session.send(Frame.Text(json))
                println("📤 Sent to $name: $json")
            } ?: println("⚠️ Car $name not connected, skipped")
        }
    }

    suspend fun updateAll(states: Map<String, CarStateEntity>) {
        println("🔄 Updating all cars: ${states.keys}")
        states.forEach { (name, state) ->
            updateCar(name, state)
        }
    }
}