package com.example.maapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.maapp.network.ServerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "ma_settings")

class SettingsRepository(
    private val context: Context,
    private val applicationScope: CoroutineScope
) {

    private val serverUrlKey = stringPreferencesKey("server_url")
    private val lastPlayerKey = stringPreferencesKey("last_player_id")

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val securePrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            "ma_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val tokenKey = "access_token"
    private val _tokenFlow = MutableStateFlow(securePrefs.getString(tokenKey, null))

    val serverConfig: StateFlow<ServerConfig?> = combine(
        context.dataStore.data.map { preferences -> preferences[serverUrlKey] },
        _tokenFlow
    ) { url, token ->
        if (url.isNullOrBlank() || token.isNullOrBlank()) null else ServerConfig(url, token)
    }.stateIn(
        scope = applicationScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val lastPlayerId: StateFlow<String?> = context.dataStore.data
        .map { preferences -> preferences[lastPlayerKey] }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun saveServer(url: String, token: String) {
        applicationScope.launch(Dispatchers.IO) {
            context.dataStore.edit { prefs ->
                prefs[serverUrlKey] = url
            }
            securePrefs.edit().putString(tokenKey, token).apply()
            _tokenFlow.value = token
        }
    }

    fun saveLastPlayer(playerId: String) {
        applicationScope.launch(Dispatchers.IO) {
            context.dataStore.edit { prefs ->
                prefs[lastPlayerKey] = playerId
            }
        }
    }
}
