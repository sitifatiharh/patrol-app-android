package com.rahma.findingpatrol.data.repository

import com.rahma.findingpatrol.data.remote.api.FindingApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class FindingRepository(private val api: FindingApi) {
    suspend fun getFindings(
        userId: Int? = null,
        patrolId: Int? = null,
        statusId: Int? = null,
        categoryId: Int? = null,
        date: String? = null,
        sectionId: Int? = null,
        priorityId: Int? = null
    ) = api.getFindings(userId, patrolId, statusId, categoryId, date, sectionId, priorityId)

    suspend fun getFindingDetail(id: Int) = api.getFindingDetail(id)

    suspend fun createFinding(
        finding: String,
        area: String,
        patrolId: Int,
        picId: Int?,
        priorityId: Int?,
        riskLevelId: Int?,
        findingTypeId: Int?,
        categoryId: Int?,
        countermeasureSuggestion: String?,
        countermeasure: String?,
        delayReason: String?,
        images: List<MultipartBody.Part>
    ) = api.createFinding(
        finding = finding.toRequestBody("text/plain".toMediaTypeOrNull()),
        area = area.toRequestBody("text/plain".toMediaTypeOrNull()),
        patrolId = patrolId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
        picId = picId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        priorityId = priorityId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        riskLevelId = riskLevelId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        findingTypeId = findingTypeId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        categoryId = categoryId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        countermeasureSuggestion = countermeasureSuggestion?.toRequestBody("text/plain".toMediaTypeOrNull()),
        countermeasure = countermeasure?.toRequestBody("text/plain".toMediaTypeOrNull()),
        delayReason = delayReason?.toRequestBody("text/plain".toMediaTypeOrNull()),
        images = images
    )

    suspend fun updateFinding(
        id: Int,
        finding: String,
        area: String,
        patrolId: Int,
        picId: Int?,
        priorityId: Int?,
        riskLevelId: Int?,
        findingTypeId: Int?,
        categoryId: Int?,
        countermeasureSuggestion: String?,
        countermeasure: String?,
        delayReason: String?,
        images: List<MultipartBody.Part>
    ) = api.updateFinding(
        id = id,
        finding = finding.toRequestBody("text/plain".toMediaTypeOrNull()),
        area = area.toRequestBody("text/plain".toMediaTypeOrNull()),
        patrolId = patrolId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
        picId = picId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        priorityId = priorityId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        riskLevelId = riskLevelId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        findingTypeId = findingTypeId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        categoryId = categoryId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        countermeasureSuggestion = countermeasureSuggestion?.toRequestBody("text/plain".toMediaTypeOrNull()),
        countermeasure = countermeasure?.toRequestBody("text/plain".toMediaTypeOrNull()),
        delayReason = delayReason?.toRequestBody("text/plain".toMediaTypeOrNull()),
        images = images
    )
}
