package com.example.maapp.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerConfig(
    val baseUrl: String,
    val token: String
) {
    val websocketUrl: String
        get() = baseUrl.replace("https://", "wss://").replace("http://", "ws://") + "/ws"
}

@Serializable
data class Player(
    val playerId: String,
    val name: String,
    val state: PlayerState,
    val volume: Int = 0,
    val currentItem: QueueItem? = null
)

@Serializable
enum class PlayerState {
    @SerialName("idle") IDLE,
    @SerialName("playing") PLAYING,
    @SerialName("paused") PAUSED,
    @SerialName("buffering") BUFFERING
}

@Serializable
data class QueueItem(
    val itemId: String,
    val title: String,
    val artist: String? = null,
    val album: String? = null,
    val durationMs: Long? = null,
    val artwork: String? = null
)

@Serializable
data class PlayerQueue(
    val playerId: String,
    val items: List<QueueItem> = emptyList(),
    val currentIndex: Int = 0
)

@Serializable
data class SearchResult(
    val itemId: String,
    val title: String,
    val artist: String? = null,
    val album: String? = null,
    val artwork: String? = null,
    val durationMs: Long? = null
)

@Serializable
data class SearchResponse(
    val items: List<SearchResult>
)

@Serializable
data class AddToQueueRequest(
    val itemId: String
)

@Serializable
sealed interface SocketEvent {
    val type: String
}

@Serializable
data class SocketMessage(
    val event: String,
    val payload: String? = null
)

@Serializable
data class PlayersResponse(val players: List<Player>)

@Serializable
data class QueuesResponse(val queues: List<PlayerQueue>)

@Serializable
data class WebSocketCommand(
    val command: String,
    val payload: Map<String, String>? = null
)
