package com.rahma.findingpatrol.data.remote.api

import com.rahma.findingpatrol.data.model.finding.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface FindingApi {

    @GET("finding")
    suspend fun getFinding(
        // General Filters
        @Query("date") date: String? = null, // Format: "YYYY-MM"
        @Query("status_id") statusId: Int? = null,
        @Query("patrol_id") patrolId: Int? = null,
        @Query("mode") mode: String,

        // User-specific Filters
        @Query("user_id") userId: Int? = null, // For "My Patrol History"

        // PIC/Section Filters
        @Query("section_id") sectionId: Int? = null, // For dropdown filter
        @Query("priority_id") priorityId: Int? = null, // For dropdown filter
        @Query("category_id") categoryId: Int? = null, // For dropdown filter
        @Query("pic_dashboard_section") picDashboardSection: Int? = null, // For PIC Dashboard
        @Query("pic_dashboard_user") picDashboardUser: Int? = null // For PIC Dashboard
    ): GetAllFinding

    @GET("finding/{id}")
    suspend fun getFindingDetail(@Path("id") id: Int): GetFindingsById

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
        @Part("countermeasure_suggestion") countermeasureSuggestion: RequestBody?,
        @Part findingPictures: List<MultipartBody.Part>
    ): CreateFindingResponse

    @Multipart
    @PUT("finding/{id}/patrol-edit")
    suspend fun patrolEditFinding(
        @Path("id") id: Int,
        @Part("patrol_id") patrolId: RequestBody?,
        @Part("finding") finding: RequestBody?,
        @Part("area") area: RequestBody?,
        @Part("pic_id") picId: RequestBody?,
        @Part("priority_id") priorityId: RequestBody?,
        @Part("risk_level_id") riskLevelId: RequestBody?,
        @Part("finding_type_id") findingTypeId: RequestBody?,
        @Part("countermeasure_suggestion") countermeasureSuggestion: RequestBody?,
        @Part findingPictures: List<MultipartBody.Part>
    ): CreateFindingResponse

    @Multipart
    @PUT("finding/{id}/assign")
    suspend fun assignPIC(
        @Path("id") id: Int,
        @Part("pic_id") picId: RequestBody?,
        @Part("patrol_id") patrolId: RequestBody?,
        @Part("finding") finding: RequestBody?,
        @Part("area") area: RequestBody?,
        @Part("priority_id") priorityId: RequestBody?,
        @Part("risk_level_id") riskLevelId: RequestBody?,
        @Part("finding_type_id") findingTypeId: RequestBody?,
        @Part("countermeasure") countermeasure: RequestBody?,
        @Part findingPictures: List<MultipartBody.Part>
    ): CreateFindingResponse

    @Multipart
    @PUT("finding/{id}/pic-update")
    suspend fun updateFindingByPIC(
        @Path("id") id: Int,
        @Part("countermeasure") countermeasure: RequestBody,
        @Part("delay_reason") delayReason: RequestBody,
        @Part improvedPictures: List<MultipartBody.Part>
    ): CreateFindingResponse

    @Multipart
    @PUT("finding/{id}/recheck")
    suspend fun recheckFinding(
        @Path("id") id: Int,
        @Part("category_id") categoryId: RequestBody?,
        @Part("pic_id") picId: RequestBody?,
        @Part("patrol_id") patrolId: RequestBody?,
        @Part("finding") finding: RequestBody?,
        @Part("area") area: RequestBody?,
        @Part("priority_id") priorityId: RequestBody?,
        @Part("risk_level_id") riskLevelId: RequestBody?,
        @Part("finding_type_id") findingTypeId: RequestBody?,
        @Part("countermeasure") countermeasure: RequestBody?,
        @Part findingPictures: List<MultipartBody.Part>,
        @Part improvedPictures: List<MultipartBody.Part>
    ): CreateFindingResponse

    @Multipart
    @PUT("finding/{id}/general-edit")
    suspend fun generalEditFinding(
        @Path("id") id: Int,
        @Part("category_id") categoryId: RequestBody?,
        @Part("pic_id") picId: RequestBody?,
        @Part("patrol_id") patrolId: RequestBody?,
        @Part("finding") finding: RequestBody?,
        @Part("area") area: RequestBody?,
        @Part("priority_id") priorityId: RequestBody?,
        @Part("risk_level_id") riskLevelId: RequestBody?,
        @Part("finding_type_id") findingTypeId: RequestBody?,
        @Part("countermeasure") countermeasure: RequestBody?,
        @Part findingPictures: List<MultipartBody.Part>,
        @Part improvedPictures: List<MultipartBody.Part>
    ): CreateFindingResponse
}
