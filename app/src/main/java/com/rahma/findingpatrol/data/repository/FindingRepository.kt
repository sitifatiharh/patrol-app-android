package com.rahma.findingpatrol.data.repository

import com.rahma.findingpatrol.data.remote.api.FindingApi
import okhttp3.MultipartBody
import okhttp3.RequestBody

class FindingRepository(
    private val api: FindingApi
) {
    suspend fun getFindings() = api.getFindings()
    
    suspend fun getFindingDetail(id: Int) = api.getFindingDetail(id)

    suspend fun createFinding(
        finding: RequestBody,
        area: RequestBody,
        patrolId: RequestBody,
        picId: RequestBody?,
        priorityId: RequestBody?,
        riskLevelId: RequestBody?,
        findingTypeId: RequestBody?,
        categoryId: RequestBody?,
        countermeasureSuggestion: RequestBody?,
        images: List<MultipartBody.Part>
    ) = api.createFinding(
        finding,
        area,
        patrolId,
        picId,
        priorityId,
        riskLevelId,
        findingTypeId,
        categoryId,
        countermeasureSuggestion,
        null, // countermeasure is null for now
        null, // delay_reason
        images
    )

    suspend fun updateFinding(
        id: Int,
        finding: RequestBody,
        area: RequestBody,
        patrolId: RequestBody,
        picId: RequestBody?,
        priorityId: RequestBody?,
        riskLevelId: RequestBody?,
        findingTypeId: RequestBody?,
        categoryId: RequestBody?,
        countermeasureSuggestion: RequestBody?,
        countermeasure: RequestBody?,
        delayReason: RequestBody?,
        images: List<MultipartBody.Part>
    ) = api.updateFinding(
        id,
        finding,
        area,
        patrolId,
        picId,
        priorityId,
        riskLevelId,
        findingTypeId,
        categoryId,
        countermeasureSuggestion,
        countermeasure,
        delayReason,
        images
    )
}
