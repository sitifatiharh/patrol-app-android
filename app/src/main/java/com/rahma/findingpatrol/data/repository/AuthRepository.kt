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

    suspend fun me() = api.me()

    suspend fun fetchAndSaveUser(): Boolean {
        return try {
            val response = api.me()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data
                // Passes all required fields to the updated saveUser function
                dataStore.saveUser(data.userId, data.role, data.sectionId, data.section)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun logout() {
        dataStore.clear()
    }
}
