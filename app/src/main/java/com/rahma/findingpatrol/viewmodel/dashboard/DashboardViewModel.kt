package com.rahma.findingpatrol.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahma.findingpatrol.data.model.dashboard.SummaryData
import com.rahma.findingpatrol.data.model.finding.Finding
import com.rahma.findingpatrol.data.repository.DashboardRepository
import com.rahma.findingpatrol.data.repository.FindingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository,
    private val findingRepository: FindingRepository
) : ViewModel() {

    private val _summaryState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val summaryState: StateFlow<DashboardState> = _summaryState

    private val _historyState = MutableStateFlow<HistoryState>(HistoryState.Loading)
    val historyState: StateFlow<HistoryState> = _historyState

    fun loadDashboard() {
        viewModelScope.launch {
            try {
                val response = dashboardRepository.getSummary() // SummaryResponse
                _summaryState.value = DashboardState.Success(response.data) // ambil .data
            } catch (e: Exception) {
                _summaryState.value = DashboardState.Error(e.message ?: "Error loading summary")
            }
        }

        viewModelScope.launch {
            try {
                val response = findingRepository.getFindings() // GetFindingResponse
                _historyState.value = HistoryState.Success(response.data) // ambil .data
            } catch (e: Exception) {
                _historyState.value = HistoryState.Error(e.message ?: "Error loading history")
            }
        }
    }

}

// Dashboard State dengan tipe data yang tepat
sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val data: SummaryData) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

// History State dengan tipe data yang tepat
sealed class HistoryState {
    object Loading : HistoryState()
    data class Success(val data: List<Finding>) : HistoryState()
    data class Error(val message: String) : HistoryState()
}
