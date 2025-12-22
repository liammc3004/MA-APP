package com.example.maapp.network

import retrofit2.http.GET

interface MaService {
    @GET("/players/all")
    suspend fun getPlayers(): PlayersResponse

    @GET("/player_queues/all")
    suspend fun getQueues(): QueuesResponse
}
