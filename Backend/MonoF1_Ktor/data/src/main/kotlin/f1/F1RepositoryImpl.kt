package f1

import f1.entity.F1Entity
import f1.entity.NegotiateEntity
import f1.request.ConnectRequest
import f1.request.NegociationRequest
import f1.response.F1DataDto
import f1.response.NegociationResponseDto
import f1.response.toEntity
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.URLEncoder

class F1RepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json,
) : F1Repository {

    override suspend fun negotiate(
        negociationRequest: NegociationRequest
    ): Result<NegotiateEntity> = withContext(
        Dispatchers.IO
    ) {
        try {
            val response = httpClient.post("https://livetiming.formula1.com/signalr/negotiate") {
                parameter("connectionData", negociationRequest.connectionData)
                parameter("clientProtocol", negociationRequest.clientProtocol)
            }.body<NegociationResponseDto>()

            Result.success(response.toEntity())

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun connect(connectRequest: ConnectRequest): Flow<Result<F1Entity>> = callbackFlow {
        val encodedToken = URLEncoder.encode(connectRequest.connectionToken, "UTF-8")

        val job = launch {
            try {
                val session = httpClient.webSocketSession(
                    urlString = "wss://livetiming.formula1.com/signalr/connect?" +
                            "transport=webSockets&" +
                            "connectionToken=$encodedToken&" +
                            "connectionData=%5B%7B%22name%22%3A%22streaming%22%7D%5D&" +
                            "clientProtocol=1.5"
                ) {
                    header("User-Agent", "BestHTTP")
                }

                val topicsJson = connectRequest.subscribeRequest.topics.joinToString(",") { "\"$it\"" }
                session.send(Frame.Text("""{"H":"streaming","M":"Subscribe","A":[[$topicsJson]],"I":1}"""))

                for (frame in session.incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        try {
                            val dto = json.decodeFromString<F1DataDto>(text)
                            if (dto.data != null) {
                                trySend(Result.success(dto.toEntity()))
                            }
                        } catch (e: Exception) {
                            // Ignore parse errors
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("F1 WebSocket error: ${e.message}")
                trySend(Result.failure(e))
            }
        }

        awaitClose { job.cancel() }
    }
}