package com.rahma.findingpatrol.data.local


import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("auth_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val LAST_ACTIVE = longPreferencesKey("last_active")
        val USER_ID = intPreferencesKey("user_id")
        val USER_ROLE = stringPreferencesKey("user_role")
    }

    suspend fun saveTokens(access: String, refresh: String) {
        context.dataStore.edit {
            it[ACCESS_TOKEN] = access
            it[REFRESH_TOKEN] = refresh
            it[LAST_ACTIVE] = System.currentTimeMillis()
        }
    }
    
    suspend fun saveUser(userId: Int, role: String) {
        context.dataStore.edit {
            it[USER_ID] = userId
            it[USER_ROLE] = role
        }
    }

    suspend fun getAccessToken() = context.dataStore.data.first()[ACCESS_TOKEN]
    suspend fun getRefreshToken() = context.dataStore.data.first()[REFRESH_TOKEN]

    fun getUserIdFlow() = context.dataStore.data.map { it[USER_ID] ?: -1 }
    fun getUserRoleFlow() = context.dataStore.data.map { it[USER_ROLE] ?: "" }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
