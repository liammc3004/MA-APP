package com.example.maapp

import android.app.Application
import com.example.maapp.data.SettingsRepository
import com.example.maapp.network.MaNetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MaApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val settingsRepository by lazy { SettingsRepository(this, applicationScope) }

    val networkModule by lazy {
        MaNetworkModule(
            context = this,
            settingsRepository = settingsRepository,
            applicationScope = applicationScope
        )
    }
}
