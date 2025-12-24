package com.rahma.findingpatrol.data.model.finding

import com.google.gson.annotations.SerializedName

data class GetFindingResponse(
    val status: String,
    val message: String,
    val data: List<Finding>
)

data class Finding(
    @SerializedName("finding_id") val findingId: Int,
    val finding: String,
    val countermeasure: String?,
    val area: String,
    @SerializedName("status_id") val statusId: Int,
    @SerializedName("priority_id") val priorityId: Int?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("finding_date") val findingDate: String?,
    @SerializedName("due_date") val dueDate: String?,
    @SerializedName("finish_date") val finishDate: String?,
    @SerializedName("recheck_date") val recheckDate: String?,
    @SerializedName("patrol_name") val patrolName: String,
    @SerializedName("status_name") val statusName: String,
    @SerializedName("priority_name") val priorityName: String?,
    @SerializedName("card_image") val cardImage: String?,
    val images: List<Image>? = emptyList()
)
