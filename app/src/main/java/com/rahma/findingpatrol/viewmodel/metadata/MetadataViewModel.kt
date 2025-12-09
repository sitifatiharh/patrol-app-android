package com.rahma.findingpatrol.viewmodel.metadata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahma.findingpatrol.data.model.metadata.MetadataData
import com.rahma.findingpatrol.data.repository.MetadataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MetadataState {
    object Loading : MetadataState()
    data class Success(val data: MetadataData) : MetadataState()
    data class Error(val message: String) : MetadataState()
}

class MetadataViewModel(private val repository: MetadataRepository) : ViewModel() {
    private val _metadataState = MutableStateFlow<MetadataState>(MetadataState.Loading)
    val metadataState: StateFlow<MetadataState> = _metadataState

    fun loadMetadata() {
        viewModelScope.launch {
            try {
                _metadataState.value = MetadataState.Loading
                val response = repository.getMetadata()
                if (response.status == "success") {
                    _metadataState.value = MetadataState.Success(response.data)
                } else {
                    _metadataState.value = MetadataState.Error(response.message)
                }
            } catch (e: Exception) {
                _metadataState.value = MetadataState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}
