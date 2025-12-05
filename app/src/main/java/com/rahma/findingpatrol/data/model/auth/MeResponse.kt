package com.rahma.findingpatrol.data.model.auth

data class MeResponse(
    val status: String,
    val data: MeData
)

data class MeData(
    val userId: Int,
    val role: String,
    val sectionId: Int,
    val iat: Long,
    val exp: Long
)