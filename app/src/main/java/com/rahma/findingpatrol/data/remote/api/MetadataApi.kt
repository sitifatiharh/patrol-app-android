package com.rahma.findingpatrol.data.remote.api

import com.rahma.findingpatrol.data.model.metadata.MetadataResponse
import retrofit2.http.GET

interface MetadataApi {
    @GET("metadata")
    suspend fun getMetadata(): MetadataResponse
}
