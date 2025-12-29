package com.rahma.findingpatrol.data.repository

import com.rahma.findingpatrol.data.remote.api.FindingApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class FindingRepository(private val api: FindingApi) {

    private fun String.toPlainRequestBody() = this.toRequestBody("text/plain".toMediaTypeOrNull())
    private fun Int.toPlainRequestBody() = this.toString().toPlainRequestBody()

    // Disesuaikan dengan FindingApi (categoryId & priorityId dihapus, ditambahkan parameter baru)
    suspend fun getFinding(
        mode: String,
        date: String? = null,
        statusId: Int? = null,
        patrolId: Int? = null,
        userId: Int? = null,
        sectionId: Int? = null,
        priorityId: Int? = null,
        categoryId: Int? = null,
        picId: Int? = null,
        picDashboardSection: Int? = null
    ) = api.getFinding(
        mode = mode,
        date = date,
        statusId = statusId,
        patrolId = patrolId,
        userId = userId,
        sectionId = sectionId,
        priorityId = priorityId,
        categoryId = categoryId,
        picDashboardUser = picId,
        picDashboardSection = picDashboardSection
    )

    suspend fun getFindingDetail(id: Int) = api.getFindingDetail(id)

    // Sesuai dengan FindingApi
    suspend fun createFinding(
        finding: String,
        area: String,
        patrolId: Int,
        picId: Int?,
        priorityId: Int?,
        riskLevelId: Int?,
        findingTypeId: Int?,
        countermeasureSuggestion: String?,
        images: List<MultipartBody.Part>
    ) = api.createFinding(
        finding = finding.toPlainRequestBody(),
        area = area.toPlainRequestBody(),
        patrolId = patrolId.toPlainRequestBody(),
        picId = picId?.toPlainRequestBody(),
        priorityId = priorityId?.toPlainRequestBody(),
        riskLevelId = riskLevelId?.toPlainRequestBody(),
        findingTypeId = findingTypeId?.toPlainRequestBody(),
        countermeasureSuggestion = countermeasureSuggestion?.toPlainRequestBody(),
        findingPictures = images
    )

    // Disesuaikan dengan FindingApi (categoryId dihapus)
    suspend fun patrolEditFinding(
        id: Int,
        finding: String?,
        area: String?,
        patrolId: Int?,
        picId: Int?,
        priorityId: Int?,
        riskLevelId: Int?,
        findingTypeId: Int?,
        countermeasureSuggestion: String?,
        images: List<MultipartBody.Part>
    ) = api.patrolEditFinding(
        id = id,
        finding = finding?.toPlainRequestBody(),
        area = area?.toPlainRequestBody(),
        patrolId = patrolId?.toPlainRequestBody(),
        picId = picId?.toPlainRequestBody(),
        priorityId = priorityId?.toPlainRequestBody(),
        riskLevelId = riskLevelId?.toPlainRequestBody(),
        findingTypeId = findingTypeId?.toPlainRequestBody(),
        countermeasureSuggestion = countermeasureSuggestion?.toPlainRequestBody(),
        findingPictures = images
    )

    // Disesuaikan dengan FindingApi (ditambahkan parameter sesuai backend)
    suspend fun assignPIC(
        id: Int,
        picId: Int?,
        patrolId: Int?,
        finding: String?,
        area: String?,
        priorityId: Int?,
        riskLevelId: Int?,
        findingTypeId: Int?,
        countermeasure: String?,
        findingPictures: List<MultipartBody.Part>
    ) = api.assignPIC(
        id = id,
        picId = picId?.toPlainRequestBody(),
        patrolId = patrolId?.toPlainRequestBody(),
        finding = finding?.toPlainRequestBody(),
        area = area?.toPlainRequestBody(),
        priorityId = priorityId?.toPlainRequestBody(),
        riskLevelId = riskLevelId?.toPlainRequestBody(),
        findingTypeId = findingTypeId?.toPlainRequestBody(),
        countermeasure = countermeasure?.toPlainRequestBody(),
        findingPictures = findingPictures
    )

    // Disesuaikan dengan FindingApi (dueDate -> delayReason)
    suspend fun updateFindingByPIC(
        id: Int,
        countermeasure: String,
        delayReason: String,
        improvedPictures: List<MultipartBody.Part>
    ) = api.updateFindingByPIC(
        id = id,
        countermeasure = countermeasure.toPlainRequestBody(),
        delayReason = delayReason.toPlainRequestBody(),
        improvedPictures = improvedPictures
    )

    // Disesuaikan dengan FindingApi (parameter diubah total)
    suspend fun recheckFinding(
        id: Int,
        categoryId: Int?,
        picId: Int?,
        patrolId: Int?,
        finding: String?,
        area: String?,
        priorityId: Int?,
        riskLevelId: Int?,
        findingTypeId: Int?,
        countermeasure: String?,
        findingPictures: List<MultipartBody.Part>,
        improvedPictures: List<MultipartBody.Part>
    ) = api.recheckFinding(
        id = id,
        categoryId = categoryId?.toPlainRequestBody(),
        picId = picId?.toPlainRequestBody(),
        patrolId = patrolId?.toPlainRequestBody(),
        finding = finding?.toPlainRequestBody(),
        area = area?.toPlainRequestBody(),
        priorityId = priorityId?.toPlainRequestBody(),
        riskLevelId = riskLevelId?.toPlainRequestBody(),
        findingTypeId = findingTypeId?.toPlainRequestBody(),
        countermeasure = countermeasure?.toPlainRequestBody(),
        findingPictures = findingPictures,
        improvedPictures = improvedPictures
    )

    // Disesuaikan dengan FindingApi
    suspend fun generalEditFinding(
        id: Int,
        categoryId: Int?,
        picId: Int?,
        patrolId: Int?,
        finding: String?,
        area: String?,
        priorityId: Int?,
        riskLevelId: Int?,
        findingTypeId: Int?,
        countermeasure: String?,
        findingPictures: List<MultipartBody.Part>,
        improvedPictures: List<MultipartBody.Part>
    ) = api.generalEditFinding(
        id = id,
        categoryId = categoryId?.toPlainRequestBody(),
        picId = picId?.toPlainRequestBody(),
        patrolId = patrolId?.toPlainRequestBody(),
        finding = finding?.toPlainRequestBody(),
        area = area?.toPlainRequestBody(),
        priorityId = priorityId?.toPlainRequestBody(),
        riskLevelId = riskLevelId?.toPlainRequestBody(),
        findingTypeId = findingTypeId?.toPlainRequestBody(),
        countermeasure = countermeasure?.toPlainRequestBody(),
        findingPictures = findingPictures,
        improvedPictures = improvedPictures
    )
}
