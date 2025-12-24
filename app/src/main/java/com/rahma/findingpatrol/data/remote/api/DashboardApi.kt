package com.rahma.findingpatrol.data.remote.api

import com.rahma.findingpatrol.data.model.dashboard.SummaryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DashboardApi {

    @GET("dashboard/summary")
    suspend fun getSummary(
        @Query("user_id") userId: Int?,
        @Query("section_id") sectionId: Int?
    ): SummaryResponse
}
