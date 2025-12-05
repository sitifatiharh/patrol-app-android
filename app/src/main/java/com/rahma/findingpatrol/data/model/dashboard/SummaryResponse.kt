package com.rahma.findingpatrol.data.model.dashboard

import com.google.gson.annotations.SerializedName

data class SummaryResponse(
    val status: String,
    val data: SummaryData
)

data class SummaryData(
    val open: Int,
    val assigned: Int,
    @SerializedName("ready_for_recheck") val readyForRecheck: Int,
    val closed: ClosedStats,
    @SerializedName("filter_applied") val filterApplied: FilterApplied
)

data class ClosedStats(
    val today: Int,
    val month: Int,
    val display: String
)

data class FilterApplied(
    val start: String,
    val end: String,
    @SerializedName("is_default") val isDefault: Boolean,
)
