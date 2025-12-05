package com.rahma.findingpatrol.ui.finding

import android.annotation.SuppressLint
import com.rahma.findingpatrol.data.model.finding.Finding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class FindingCardData(
    val id: Int,
    val patrol: String,
    val area: String,
    val title: String,
    val image: String,
    val status: String,
    val priority: String?,
    val dateLabel: String,
    val dateValue: String
)

@SuppressLint("NewApi")
fun Finding.toCardData(): FindingCardData {
    val formatter = DateTimeFormatter.ISO_DATE_TIME

    val (label, date) = when (statusId) {

        1, 2 -> { // Open & Assigned → due or overdue
            if (dueDate != null) {
                val due = LocalDate.parse(dueDate, formatter)
                val today = LocalDate.now()
                if (today.isAfter(due))
                    "Overdue" to dueDate
                else
                    "Due" to dueDate
            } else {
                "Due" to ""
            }
        }

        3 -> "Fixed" to (finishDate ?: "")
        4 -> "Checked" to (recheckDate ?: "")
        else -> "" to ""
    }

    val title = when (statusId) {
        3, 4 -> "$finding. $countermeasure"
        else -> finding
    }

    return FindingCardData(
        id = findingId,
        title = title,
        patrol = patrolName,
        area = area,
        image = cardImage,
        status = statusName,
        priority = priorityName.ifEmpty { null },
        dateLabel = label,
        dateValue = date
    )
}
