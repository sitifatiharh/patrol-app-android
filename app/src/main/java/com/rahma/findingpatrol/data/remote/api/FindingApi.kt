package com.rahma.findingpatrol.data.remote.api

import com.rahma.findingpatrol.data.model.finding.CreateFindingResponse
import com.rahma.findingpatrol.data.model.finding.FindingDetailResponse
import com.rahma.findingpatrol.data.model.finding.GetFindingResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface FindingApi {

    @GET("finding")
    suspend fun getFindings(
        @Query("user_id") userId: Int? = null,
        @Query("patrol_id") patrolId: Int? = null,
        @Query("status_id") statusId: Int? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("date") date: String? = null,
        @Query("section_id") sectionId: Int? = null,
        @Query("priority_id") priorityId: Int? = null
    ): GetFindingResponse

    @GET("finding/{id}")
    suspend fun getFindingDetail(@Path("id") id: Int): FindingDetailResponse

    @Multipart
    @POST("finding")
    suspend fun createFinding(
        @Part("finding") finding: RequestBody,
        @Part("area") area: RequestBody,
        @Part("patrol_id") patrolId: RequestBody,
        @Part("pic_id") picId: RequestBody?,
        @Part("priority_id") priorityId: RequestBody?,
        @Part("risk_level_id") riskLevelId: RequestBody?,
        @Part("finding_type_id") findingTypeId: RequestBody?,
        @Part("category_id") categoryId: RequestBody?,
        @Part("countermeasure_suggestion") countermeasureSuggestion: RequestBody?,
        @Part("countermeasure") countermeasure: RequestBody?,
        @Part("delay_reason") delayReason: RequestBody?,
        @Part images: List<MultipartBody.Part>
    ): CreateFindingResponse

    @Multipart
    @PUT("finding/{id}/patrol-edit")
    suspend fun updateFinding(
        @Path("id") id: Int,
        @Part("finding") finding: RequestBody,
        @Part("area") area: RequestBody,
        @Part("patrol_id") patrolId: RequestBody,
        @Part("pic_id") picId: RequestBody?,
        @Part("priority_id") priorityId: RequestBody?,
        @Part("risk_level_id") riskLevelId: RequestBody?,
        @Part("finding_type_id") findingTypeId: RequestBody?,
        @Part("category_id") categoryId: RequestBody?,
        @Part("countermeasure_suggestion") countermeasureSuggestion: RequestBody?,
        @Part("countermeasure") countermeasure: RequestBody?,
        @Part("delay_reason") delayReason: RequestBody?,
        @Part images: List<MultipartBody.Part>
    ): CreateFindingResponse
}
