package com.example.maapp.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MaService {
    @GET("/players/all")
    suspend fun getPlayers(): PlayersResponse

    @GET("/player_queues/all")
    suspend fun getQueues(): QueuesResponse

    @GET("/library/search")
    suspend fun search(@Query("query") query: String): SearchResponse

    @POST("/player_queues/{playerId}/add")
    suspend fun addToQueue(
        @Path("playerId") playerId: String,
        @Body request: AddToQueueRequest
    )
}
