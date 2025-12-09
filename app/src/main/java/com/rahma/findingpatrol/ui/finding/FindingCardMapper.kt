package com.rahma.findingpatrol.ui.finding

import android.annotation.SuppressLint
import com.rahma.findingpatrol.data.model.finding.Finding
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    // Formatter untuk parsing input dari API (ISO 8601: 2025-01-25T00:00:00.000Z)
    val inputFormatter = DateTimeFormatter.ISO_DATE_TIME
    
    // Formatter untuk output ke UI (contoh: 25 Feb 2025)
    val outputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "-"
        return try {
            val date = LocalDateTime.parse(dateString, inputFormatter).toLocalDate()
            date.format(outputFormatter)
        } catch (_: Exception) {
            dateString // Return raw string jika parsing gagal
        }
    }

    val (label, date) = when (statusId) {
        1, 2 -> { // Open & Assigned -> Check Due Date
            if (!dueDate.isNullOrEmpty()) {
                try {
                    val dueDateTime = LocalDateTime.parse(dueDate, inputFormatter).toLocalDate()
                    val today = LocalDate.now()
                    
                    val formattedDate = dueDateTime.format(outputFormatter)
                    
                    if (today.isAfter(dueDateTime)) {
                        "Overdue" to formattedDate
                    } else {
                        "Due" to formattedDate
                    }
                } catch (_: Exception) {
                    "Due" to dueDate
                }
            } else {
                "Due" to "-"
            }
        }
        3 -> "Fixed" to formatDate(finishDate) // Fixed -> Finish Date
        4 -> "Checked" to formatDate(recheckDate) // Closed/Checked -> Recheck Date
        else -> "" to ""
    }

    // Handle null countermeasure cleanly
    val title = when (statusId) {
        3, 4 -> "$finding. ${countermeasure ?: ""}"
        else -> finding
    }

    // Fix NPE on priorityName and patrolName
    return FindingCardData(
        id = findingId,
        title = title,
        patrol = patrolName ?: "-",
        area = area,
        image = cardImage,
        status = statusName,
        priority = if (priorityName.isNullOrEmpty()) "Low" else priorityName,
        dateLabel = label,
        dateValue = date
    )
}
