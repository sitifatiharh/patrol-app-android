package com.rahma.findingpatrol.data.repository

import com.rahma.findingpatrol.data.remote.api.DashboardApi

class DashboardRepository(
    private val api: DashboardApi
) {
    suspend fun getSummary(userId: Int?, sectionId: Int?) = api.getSummary(userId, sectionId)
}