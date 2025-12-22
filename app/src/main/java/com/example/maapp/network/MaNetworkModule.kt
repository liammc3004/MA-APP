package com.example.maapp.network

import android.content.Context
import com.example.maapp.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.MediaType.Companion.toMediaType

class MaNetworkModule(
    context: Context,
    private val settingsRepository: SettingsRepository,
    private val applicationScope: CoroutineScope
) {

    private val json = Json { ignoreUnknownKeys = true }

    private val authInterceptor = Interceptor { chain ->
        val cfg = settingsRepository.serverConfig.value
        val original = chain.request()
        val builder = original.newBuilder()
        cfg?.let { builder.addHeader("Authorization", "Bearer ${it.token}") }
        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private var retrofit: Retrofit? = null

    val webSocketClient: MaWebSocketClient by lazy {
        MaWebSocketClient(okHttpClient, json, applicationScope)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun retrofitService(): MaService? {
        val config = settingsRepository.serverConfig.value ?: return null
        if (retrofit == null || retrofit?.baseUrl()?.toString()?.contains(config.baseUrl) == false) {
            retrofit = Retrofit.Builder()
                .baseUrl(ensureTrailingSlash(config.baseUrl))
                .client(okHttpClient)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
        }
        return retrofit?.create(MaService::class.java)
    }

    fun reconnectWebSocket() {
        applicationScope.launch(Dispatchers.IO) {
            val cfg = settingsRepository.serverConfig.filterNotNull().first()
            webSocketClient.connect(cfg)
        }
    }

    private fun ensureTrailingSlash(baseUrl: String): String =
        if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
}
