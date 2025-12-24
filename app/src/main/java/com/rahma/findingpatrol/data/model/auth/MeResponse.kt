package com.rahma.findingpatrol.data.model.auth

data class MeResponse(
    val status: String,
    val data: MeData
)

data class MeData(
    val userId: Int,
    val role: String,
    val sectionId: Int,
    val section: String, // Added the missing field
    val iat: Long,
    val exp: Long
)
