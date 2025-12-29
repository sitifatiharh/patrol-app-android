package com.rahma.findingpatrol.viewmodel.finding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahma.findingpatrol.data.model.finding.AllFinding
import com.rahma.findingpatrol.data.model.metadata.Category
import com.rahma.findingpatrol.data.model.metadata.Patrol
import com.rahma.findingpatrol.data.model.metadata.Priority
import com.rahma.findingpatrol.data.model.metadata.Status
import com.rahma.findingpatrol.data.repository.FindingRepository
import com.rahma.findingpatrol.data.repository.MetadataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FindingHistoryViewModel(
    private val findingRepository: FindingRepository,
    private val metadataRepository: MetadataRepository
) : ViewModel() {

    private val _historyState = MutableStateFlow<HistoryState>(HistoryState.Loading)
    val historyState: StateFlow<HistoryState> = _historyState

    private val _patrols = MutableStateFlow<List<Patrol>>(emptyList())
    val patrols: StateFlow<List<Patrol>> = _patrols

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _priorities = MutableStateFlow<List<Priority>>(emptyList())
    val priorities: StateFlow<List<Priority>> = _priorities

    private val _statuses = MutableStateFlow<List<Status>>(emptyList())
    val statuses: StateFlow<List<Status>> = _statuses

    fun loadFindingsForStats(
        userId: Int,
        userMode: String,
        sectionId: Int,
        statusId: Int,
        patrolId: Int? = null,
        date: String? = null
    ) {
        _historyState.value = HistoryState.Loading
        viewModelScope.launch {
            try {
                var queryUserId: Int? = null
                var querySectionId: Int? = null

                val apiMode = if (userMode == "patrol") "patrol" else "job"

                when (userMode) {
                    "patrol" -> {
                        if (statusId != 4) { // Not Closed
                            queryUserId = userId
                        }
                    }
                    "pic" -> {
                        // Hanya filter section jika status Open, Assigned, atau Ready
                        if (statusId in 1..3) {
                            querySectionId = sectionId
                        }
                        // Jika status 4 (Closed), querySectionId tetap null (Show All)
                    }
                    "improvement" -> {
                        // Improvement role sees all, so all query params are null
                    }
                }

                val response = findingRepository.getFinding(
                    mode = apiMode,
                    userId = queryUserId,
                    sectionId = querySectionId, // Correctly pass sectionId for PIC mode
                    picId = null, // This should not be used for this screen's logic
                    patrolId = patrolId,
                    statusId = statusId,
                    date = date
                )
                _historyState.value = HistoryState.Success(response.data)
            } catch (e: Exception) {
                Log.e("FindingHistoryVM", "Error loading findings for stats", e)
                _historyState.value = HistoryState.Error(e.message ?: "Gagal memuat temuan statistik")
            }
        }
    }

    //This is for the preview on the dashboard, it should probably be simplified
    fun loadFindings(
        mode: String,
        userId: Int? = null,
        patrolId: Int? = null,
        statusId: Int? = null,
        priorityId: Int? = null,
        categoryId: Int? = null
    ) {
        _historyState.value = HistoryState.Loading
        viewModelScope.launch {
            try {
                val response = findingRepository.getFinding(
                    mode = mode,
                    userId = userId,
                    patrolId = patrolId,
                    statusId = statusId,
                    priorityId = priorityId,
                    categoryId = categoryId
                )
                _historyState.value = HistoryState.Success(response.data)
            } catch (e: Exception) {
                Log.e("FindingHistoryVM", "Error loading findings", e)
                _historyState.value = HistoryState.Error(e.message ?: "Gagal memuat temuan")
            }
        }
    }

    fun loadPatrols() {
        viewModelScope.launch {
            try {
                val response = metadataRepository.getMetadata()
                _patrols.value = response.data.patrol
            } catch (e: Exception) {
                Log.d("FindingHistoryVM", "Failed to load patrols: ${e.message}")
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val response = metadataRepository.getMetadata()
                _categories.value = response.data.category
            } catch (e: Exception) {
                Log.d("FindingHistoryVM", "Failed to load categories: ${e.message}")
            }
        }
    }

    fun loadPriorities() {
        viewModelScope.launch {
            try {
                val response = metadataRepository.getMetadata()
                _priorities.value = response.data.priority
            } catch (e: Exception) {
                Log.d("FindingHistoryVM", "Failed to load priorities: ${e.message}")
            }
        }
    }

    fun loadStatuses() {
        viewModelScope.launch {
            try {
                val response = metadataRepository.getMetadata()
                _statuses.value = response.data.status
            } catch (e: Exception) {
                Log.d("FindingHistoryVM", "Failed to load statuses: ${e.message}")
            }
        }
    }
}

sealed class HistoryState {
    object Loading : HistoryState()
    data class Success(val data: List<AllFinding>) : HistoryState()
    data class Error(val message: String) : HistoryState()
}

fun filterAndSortFindings(findings: List<AllFinding>, sortOption: String): List<AllFinding> {
    return when (sortOption) {
        "Prioritas" -> findings.sortedBy { it.priorityName } // Note: Sorting by name, not ID.
        "Status" -> findings.sortedBy { it.statusId }
        else -> findings.sortedByDescending { it.dueDate } // Default sort by due date
    }
}
