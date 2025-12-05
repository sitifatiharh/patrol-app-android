package com.rahma.findingpatrol.data.remote.api

import com.rahma.findingpatrol.data.model.auth.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST ("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<RefreshResponse>

    @GET("auth/me")
    suspend fun me(): Response<MeResponse>
}