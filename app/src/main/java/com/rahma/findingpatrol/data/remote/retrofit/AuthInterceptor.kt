package com.rahma.findingpatrol.data.remote.retrofit

import com.rahma.findingpatrol.data.local.DataStoreManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val dataStore: DataStoreManager,
    private val refreshFunction: suspend (String) -> Boolean
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val access = runBlocking { dataStore.getAccessToken() }
        val request = chain.request()

        val newReq = request.newBuilder()
            .apply {
                if (!access.isNullOrEmpty()) {
                    header("Authorization", "Bearer $access")
                }
            }.build()

        val response = chain.proceed(newReq)

        // If 401 → try refresh
        if (response.code == 401) {
            val refresh = runBlocking { dataStore.getRefreshToken() }
            if (!refresh.isNullOrEmpty()) {
                val refreshed = runBlocking { refreshFunction(refresh) }

                if (refreshed) {
                    val newAccess = runBlocking { dataStore.getAccessToken() }
                    val retryReq = request.newBuilder()
                        .header("Authorization", "Bearer $newAccess")
                        .build()
                    return chain.proceed(retryReq)
                }
            }
        }

        return response
    }
}
