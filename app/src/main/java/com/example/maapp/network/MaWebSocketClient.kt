package com.example.maapp.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class MaWebSocketClient(
    private val okHttpClient: okhttp3.OkHttpClient,
    private val json: Json,
    private val scope: CoroutineScope
) : WebSocketListener() {

    private var webSocket: WebSocket? = null
    private var config: ServerConfig? = null

    private val _events = MutableSharedFlow<WebSocketUpdate>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<WebSocketUpdate> = _events

    fun connect(config: ServerConfig) {
        if (this.config == config && webSocket != null) return
        this.config = config
        webSocket?.cancel()

        val request = Request.Builder()
            .url(config.websocketUrl)
            .addHeader("Authorization", "Bearer ${config.token}")
            .build()

        webSocket = okHttpClient.newWebSocket(request, this)
    }

    fun disconnect() {
        webSocket?.close(1000, "closed by client")
        webSocket = null
    }

    fun sendCommand(command: String, payload: Map<String, String>? = null) {
        val message = WebSocketCommand(command, payload)
        val body = json.encodeToString(WebSocketCommand.serializer(), message)
        webSocket?.send(body)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        scope.launch { _events.emit(WebSocketUpdate.Connected) }
        val cfg = config ?: return
        // Authenticate and request initial state
        sendCommand(
            command = "auth",
            payload = mapOf("token" to cfg.token)
        )
        sendCommand("players/all")
        sendCommand("player_queues/all")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        scope.launch(Dispatchers.IO) {
            runCatching {
                val jsonElement = json.parseToJsonElement(text)
                val rootObject = jsonElement.jsonObject
                val event = rootObject["event"]?.jsonPrimitive?.content
                val payloadElement = rootObject["payload"]

                when (event) {
                    "players/all", "players/updated" -> {
                        val response = payloadElement?.let { json.decodeFromJsonElement<PlayersResponse>(it) }
                        response?.let { _events.emit(WebSocketUpdate.Players(it.players)) }
                    }
                    "player_queues/all", "player_queues/updated" -> {
                        val response = payloadElement?.let { json.decodeFromJsonElement<QueuesResponse>(it) }
                        response?.let { _events.emit(WebSocketUpdate.Queues(it.queues)) }
                    }
                    else -> {
                        _events.emit(WebSocketUpdate.Raw(text))
                    }
                }
            }.onFailure { throwable ->
                _events.emit(WebSocketUpdate.Error(throwable.message ?: "Unknown error"))
            }
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        scope.launch { _events.emit(WebSocketUpdate.Error(t.message ?: "WebSocket failure")) }
    }
}

sealed interface WebSocketUpdate {
    data object Connected : WebSocketUpdate
    data class Players(val players: List<Player>) : WebSocketUpdate
    data class Queues(val queues: List<PlayerQueue>) : WebSocketUpdate
    data class Error(val message: String) : WebSocketUpdate
    data class Raw(val raw: String) : WebSocketUpdate
}
