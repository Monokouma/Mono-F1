package hue

import hue.dto.HueEventDto
import hue.dto.toEntity
import hue.entity.HueLightStateEntity
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class HueRepositoryImpl(
    private val dotenv: Dotenv,
    private val httpClient: HttpClient,
    private val json: Json,
) : HueRepository {


    override fun subscribeServerSentEvent(): Flow<HueLightStateEntity?> = callbackFlow {
        val url = "https://${dotenv["HUE_BRIDGE_IP"]}/eventstream/clip/v2"
        println("hue: Starting negotiate...")

        val job = launch {
            httpClient.prepareGet(url) {
                header("hue-application-key", dotenv["HUE_TOKEN"])
                header("Accept", "text/event-stream")
                timeout {
                    requestTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
                    socketTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
                }
            }.execute { response ->
                val channel = response.bodyAsChannel()

                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: continue

                    if (line.startsWith("data: ")) {
                        val jsonData = line.removePrefix("data: ")
                        try {
                            val events = json.decodeFromString<List<HueEventDto>>(jsonData)
                            trySend(events.toEntity())

                        } catch (e: Exception) {
                        }
                    }
                }
            }
        }

        awaitClose { job.cancel() }
    }.flowOn(Dispatchers.IO)
}