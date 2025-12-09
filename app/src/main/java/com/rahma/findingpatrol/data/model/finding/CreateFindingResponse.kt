package com.rahma.findingpatrol.data.model.finding

import com.google.gson.annotations.SerializedName

// Updated Response Model
data class CreateFindingResponse(
    val status: String,
    val message: String,
    val data: CreatedFindingData
)

data class CreatedFindingData(
    @SerializedName("finding_id") val findingId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("finding_date") val findingDate: String,
    val finding: String,
    @SerializedName("patrol_id") val patrolId: Int,
    val area: String,
    @SerializedName("pic_id") val picId: Int?,
    @SerializedName("priority_id") val priorityId: Int?,
    @SerializedName("risk_level_id") val riskLevelId: Int?,
    @SerializedName("finding_type_id") val findingTypeId: Int?,
    @SerializedName("countermeasure_suggestion") val countermeasureSuggestion: String?,
    val countermeasure: String?,
    @SerializedName("delay_reason") val delayReason: String?,
    @SerializedName("assigned_date") val assignedDate: String?,
    @SerializedName("due_date") val dueDate: String?,
    @SerializedName("finish_date") val finishDate: String?,
    @SerializedName("recheck_date") val recheckDate: String?,
    @SerializedName("category_id") val categoryId: Int?,
    @SerializedName("status_id") val statusId: Int,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("patrol_name") val patrolName: String?,
    @SerializedName("finding_type_name") val findingTypeName: String?,
    @SerializedName("priority_name") val priorityName: String?,
    @SerializedName("risk_level_name") val riskLevelName: String?,
    @SerializedName("status_name") val statusName: String?,
    @SerializedName("patrol_creator_name") val patrolCreatorName: String?,
    @SerializedName("pic_name") val picName: String?,
    val images: List<FindingImage>
)
