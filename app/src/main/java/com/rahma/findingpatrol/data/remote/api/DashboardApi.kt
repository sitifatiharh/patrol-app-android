package com.rahma.findingpatrol.data.remote.api

import com.rahma.findingpatrol.data.model.dashboard.SummaryResponse
import retrofit2.http.GET

interface DashboardApi {

    @GET("dashboard/summary")
    suspend fun getSummary(): SummaryResponse
}
