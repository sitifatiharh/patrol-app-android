package com.rahma.findingpatrol.viewmodel.finding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahma.findingpatrol.data.model.finding.FindingDetail
import com.rahma.findingpatrol.data.repository.FindingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class FindingDetailState {
    object Loading : FindingDetailState()
    data class Success(val data: FindingDetail) : FindingDetailState()
    data class Error(val message: String) : FindingDetailState()
}

class FindingDetailViewModel(
    private val repository: FindingRepository
) : ViewModel() {

    private val _state = MutableStateFlow<FindingDetailState>(FindingDetailState.Loading)
    val state: StateFlow<FindingDetailState> = _state

    fun getFindingDetail(id: Int) {
        viewModelScope.launch {
            _state.value = FindingDetailState.Loading
            try {
                val response = repository.getFindingDetail(id)
                // Assuming response format has status "success"
                if (response.status == "success") {
                    _state.value = FindingDetailState.Success(response.data)
                } else {
                    _state.value = FindingDetailState.Error("Failed to fetch finding detail")
                }
            } catch (e: Exception) {
                _state.value = FindingDetailState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
