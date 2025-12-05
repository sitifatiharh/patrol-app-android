package com.rahma.findingpatrol.data.model.auth

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val status: String,
    val message: String,
    val data: LoginData
)

data class LoginData(
    val user: User,
    val accessToken: String,
    val refreshToken: String
)

data class User(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("section_id") val sectionId: Int,
    val username: String,
    val role: String
)
