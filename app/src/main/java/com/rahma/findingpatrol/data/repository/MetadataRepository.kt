package com.rahma.findingpatrol.data.repository

import com.rahma.findingpatrol.data.model.metadata.MetadataResponse
import com.rahma.findingpatrol.data.remote.api.MetadataApi

class MetadataRepository(private val api: MetadataApi) {
    suspend fun getMetadata(): MetadataResponse {
        return api.getMetadata()
    }
}
