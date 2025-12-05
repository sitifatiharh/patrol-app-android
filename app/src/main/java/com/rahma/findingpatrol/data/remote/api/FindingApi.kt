package com.rahma.findingpatrol.data.remote.api

import com.rahma.findingpatrol.data.model.finding.GetFindingResponse
import retrofit2.http.GET

interface FindingApi {

    @GET("finding")
    suspend fun getFindings(): GetFindingResponse
}