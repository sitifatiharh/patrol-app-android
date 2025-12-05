package com.rahma.findingpatrol.data.repository

import com.rahma.findingpatrol.data.local.DataStoreManager
import com.rahma.findingpatrol.data.model.auth.*
import com.rahma.findingpatrol.data.remote.api.AuthApi
import retrofit2.Response

class AuthRepository(
    private val api: AuthApi,
    private val dataStore: DataStoreManager
) {
    suspend fun login(username: String, password: String): Response<LoginResponse> {
        val response = api.login(LoginRequest(username, password))
        if (response.isSuccessful) {
            response.body()?.data?.let { data ->
                dataStore.saveTokens(data.accessToken, data.refreshToken)
            }
        }
        return response
    }

    suspend fun refreshToken(refreshToken: String): Boolean {
        val response = api.refresh(RefreshRequest(refreshToken))
        if (response.isSuccessful) {
            response.body()?.data?.accessToken?.let { newToken ->
                dataStore.saveTokens(newToken, refreshToken)
                return true
            }
        }
        return false
    }

    suspend fun me() = api.me()

    suspend fun logout() {
        dataStore.clear()
    }
}
