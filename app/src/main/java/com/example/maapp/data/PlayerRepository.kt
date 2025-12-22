package com.example.maapp.data

import com.example.maapp.network.MaNetworkModule
import com.example.maapp.network.Player
import com.example.maapp.network.PlayerQueue
import com.example.maapp.network.WebSocketUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerRepository(
    private val networkModule: MaNetworkModule,
    private val scope: CoroutineScope
) {

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _queues = MutableStateFlow<List<PlayerQueue>>(emptyList())
    val queues: StateFlow<List<PlayerQueue>> = _queues.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            networkModule.webSocketClient.events.collect { event ->
                when (event) {
                    is WebSocketUpdate.Players -> _players.value = event.players
                    is WebSocketUpdate.Queues -> _queues.value = event.queues
                    else -> Unit
                }
            }
        }
    }

    fun refreshFromHttp() {
        scope.launch(Dispatchers.IO) {
            val service = networkModule.retrofitService() ?: return@launch
            runCatching { service.getPlayers() }.onSuccess { response ->
                _players.value = response.players
            }
            runCatching { service.getQueues() }.onSuccess { response ->
                _queues.value = response.queues
            }
        }
    }

    fun connectSocket() {
        scope.launch(Dispatchers.IO) {
            networkModule.reconnectWebSocket()
        }
    }
}
