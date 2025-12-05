package com.rahma.findingpatrol.data.remote.retrofit

import com.rahma.findingpatrol.data.local.DataStoreManager
import com.rahma.findingpatrol.data.remote.api.AuthApi
import com.rahma.findingpatrol.data.remote.api.DashboardApi
//import com.rahma.findingpatrol.data.remote.api.FindingApi
//import com.rahma.findingpatrol.data.remote.api.MetadataApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    lateinit var retrofit: Retrofit
        private set

    lateinit var authApi: AuthApi
        private set

    fun init(baseUrl: String, dataStore: DataStoreManager) {

        val client = OkHttpClient.Builder()
            .addInterceptor(
                AuthInterceptor(
                    dataStore,
                    refreshFunction = { refreshToken ->
                        refreshToken(refreshToken, dataStore)
                    }
                )
            )
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        authApi = retrofit.create(AuthApi::class.java)
    }

    private suspend fun refreshToken(
        refreshToken: String,
        dataStore: DataStoreManager
    ): Boolean {

        val response = authApi.refresh(
            com.rahma.findingpatrol.data.model.auth.RefreshRequest(refreshToken)
        )

        if (response.isSuccessful) {
            response.body()?.data?.accessToken?.let { newToken ->
                dataStore.saveTokens(newToken, refreshToken)
                return true
            }
        }
        return false
    }
    val dashboardApi: DashboardApi by lazy { retrofit.create(DashboardApi::class.java) }
//    val findingApi: FindingApi by lazy { retrofit.create(FindingApi::class.java) }
//    val metadataApi: MetadataApi by lazy { retrofit.create(MetadataApi::class.java) }
}
