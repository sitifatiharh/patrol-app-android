package com.rahma.findingpatrol.data.repository

import com.rahma.findingpatrol.data.remote.api.FindingApi

class FindingRepository(
    private val api: FindingApi
) {
    suspend fun getFindings() = api.getFindings()
}
