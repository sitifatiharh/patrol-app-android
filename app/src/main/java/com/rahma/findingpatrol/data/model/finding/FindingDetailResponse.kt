package com.rahma.findingpatrol.data.model.finding

import com.google.gson.annotations.SerializedName

data class FindingDetailResponse(
    val status: String,
    val data: FindingDetail
)

data class FindingDetail(
    @SerializedName("finding_id") val findingId: Int,
    @SerializedName("pic_id") val picId: Int?,
    @SerializedName("patrol_creator_id") val patrolCreatorId: Int?,
    @SerializedName("finding") val finding: String,
    @SerializedName("area") val area: String,
    @SerializedName("patrol_name") val patrolName: String?,
    @SerializedName("created_by") val createdBy: String?,
    @SerializedName("patrol_creator_name") val patrolCreatorName: String?,
    @SerializedName("pic_name") val picName: String?,
    @SerializedName("status_name") val statusName: String,
    @SerializedName("priority_name") val priorityName: String?,
    @SerializedName("risk_level_name") val riskLevelName: String?,
    @SerializedName("finding_type_name") val findingTypeName: String?,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("countermeasure_suggestion") val countermeasureSuggestion: String?,
    @SerializedName("countermeasure") val countermeasure: String?,
    @SerializedName("delay_reason") val delayReason: String?,
    @SerializedName("finding_date") val findingDate: String?,
    @SerializedName("assigned_date") val assignedDate: String?,
    @SerializedName("due_date") val dueDate: String?,
    @SerializedName("finish_date") val finishDate: String?,
    @SerializedName("recheck_date") val recheckDate: String?,
    @SerializedName("lt_day") val ltDay: Int,
    @SerializedName("images") val images: List<Image>
)

// Unified Image data class
data class Image(
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("image_type") val imageType: String
)
