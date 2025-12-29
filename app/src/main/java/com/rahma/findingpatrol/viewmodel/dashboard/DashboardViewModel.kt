package com.rahma.findingpatrol.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahma.findingpatrol.data.local.DataStoreManager
import com.rahma.findingpatrol.data.model.dashboard.SummaryData
import com.rahma.findingpatrol.data.model.finding.AllFinding
import com.rahma.findingpatrol.data.repository.DashboardRepository
import com.rahma.findingpatrol.data.repository.FindingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository,
    private val findingRepository: FindingRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _summaryState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val summaryState: StateFlow<DashboardState> = _summaryState

    private val _historyState = MutableStateFlow<HistoryState>(HistoryState.Loading)
    val historyState: StateFlow<HistoryState> = _historyState

    fun loadDashboard(userId: Int, sectionId: Int, mode: String) {
        // Load Summary based on the selected mode
        viewModelScope.launch {
            _summaryState.value = DashboardState.Loading
            try {
                val summaryUserId = if (mode == "patrol") userId else null
                val summarySectionId = if (mode == "pic") sectionId else null

                val response = dashboardRepository.getSummary(
                    userId = summaryUserId,
                    sectionId = summarySectionId
                )
                _summaryState.value = DashboardState.Success(response.data)
            } catch (e: Exception) {
                _summaryState.value = DashboardState.Error(e.message ?: "Error loading summary")
            }
        }

        // Load History preview, ALWAYS based on the user's own findings
        viewModelScope.launch {
            _historyState.value = HistoryState.Loading
            try {
                val response = findingRepository.getFinding(
                    mode = "patrol",
                    userId = userId, // Always use userId for the preview
                    sectionId = null // Ensure sectionId is not used here
                )
                _historyState.value = HistoryState.Success(response.data)
            } catch (e: Exception) {
                _historyState.value = HistoryState.Error(e.message ?: "Error loading history")
            }
        }
    }

    fun saveUserMode(mode: String) {
        viewModelScope.launch {
            dataStoreManager.saveUserMode(mode)
        }
    }
}

// Dashboard State
sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val data: SummaryData) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

// History State
sealed class HistoryState {
    object Loading : HistoryState()
    data class Success(val data: List<AllFinding>) : HistoryState()
    data class Error(val message: String) : HistoryState()
}
