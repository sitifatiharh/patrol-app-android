package com.rahma.findingpatrol.data.model.metadata

import com.google.gson.annotations.SerializedName

data class MetadataResponse(
    val status: String,
    val message: String,
    val data: MetadataData
)

data class MetadataData(
    val patrol: List<Patrol>,
    val category: List<Category>,
    val pics: List<Pic>,
    val priority: List<Priority>,
    @SerializedName("risk_level") val riskLevel: List<RiskLevel>,
    @SerializedName("finding_type") val findingType: List<FindingType>,
    val status: List<Status>
)

data class Patrol(
    @SerializedName("patrol_id") val patrolId: Int,
    @SerializedName("patrol_name") val patrolName: String
)

data class Category(
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("category_name") val categoryName: String
)

data class Pic(
    @SerializedName("section_id") val sectionId: Int,
    @SerializedName("section_name") val sectionName: String
)

data class Priority(
    @SerializedName("priority_id") val priorityId: Int,
    @SerializedName("priority_name") val priorityName: String
)

data class RiskLevel(
    @SerializedName("risk_level_id") val riskLevelId: Int,
    @SerializedName("risk_level") val riskLevel: String
)

data class FindingType(
    @SerializedName("finding_type_id") val findingTypeId: Int,
    @SerializedName("type_name") val typeName: String
)

data class Status(
    @SerializedName("status_id") val statusId: Int,
    @SerializedName("status_name") val statusName: String
)
