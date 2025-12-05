package com.rahma.findingpatrol.data.local


import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore("auth_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val LAST_ACTIVE = longPreferencesKey("last_active")
    }

    suspend fun saveTokens(access: String, refresh: String) {
        context.dataStore.edit {
            it[ACCESS_TOKEN] = access
            it[REFRESH_TOKEN] = refresh
            it[LAST_ACTIVE] = System.currentTimeMillis()
        }
    }

    suspend fun updateLastActive() {
        context.dataStore.edit {
            it[LAST_ACTIVE] = System.currentTimeMillis()
        }
    }

    suspend fun getAccessToken() = context.dataStore.data.first()[ACCESS_TOKEN]
    suspend fun getRefreshToken() = context.dataStore.data.first()[REFRESH_TOKEN]
    suspend fun getLastActive() = context.dataStore.data.first()[LAST_ACTIVE] ?: 0L

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
