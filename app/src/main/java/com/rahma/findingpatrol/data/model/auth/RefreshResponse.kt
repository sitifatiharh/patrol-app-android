package com.rahma.findingpatrol.data.model.auth

data class RefreshResponse(
    val status: String,
    val message: String,
    val data: RefreshData
)

data class RefreshData(
    val accessToken: String
)