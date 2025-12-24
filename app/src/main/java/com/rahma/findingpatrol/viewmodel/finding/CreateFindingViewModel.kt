package com.rahma.findingpatrol.viewmodel.finding

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahma.findingpatrol.data.repository.FindingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class CreateFindingViewModel(
    private val repository: FindingRepository
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _createdFindingId = MutableStateFlow<Int?>(null)
    val createdFindingId: StateFlow<Int?> = _createdFindingId

    fun resetState() {
        _success.value = false
        _loading.value = false
        _error.value = null
        _createdFindingId.value = null
    }

    fun createFinding(
        context: Context,
        finding: String,
        area: String,
        patrolId: Int,
        picId: Int?,
        priorityId: Int?,
        riskLevelId: Int?,
        findingTypeId: Int?,
        categoryId: Int?,
        countermeasureSuggestion: String?,
        imageUris: List<Uri>
    ) {
        submitFinding(
            context, 
            isUpdate = false, 
            id = 0, 
            finding, area, patrolId, picId, priorityId, riskLevelId, findingTypeId, categoryId, 
            countermeasureSuggestion, null, null, 
            imageUris, 
            emptyList()
        )
    }

    fun updateFinding(
        context: Context,
        id: Int,
        finding: String = "",
        area: String = "",
        patrolId: Int = 0,
        picId: Int? = null,
        priorityId: Int? = null,
        riskLevelId: Int? = null,
        findingTypeId: Int? = null,
        categoryId: Int? = null,
        countermeasureSuggestion: String? = null,
        countermeasure: String? = null, 
        delayReason: String? = null, 
        imageUris: List<Uri> = emptyList(),
        newImprovedImageUris: List<Uri> = emptyList()
    ) {
        submitFinding(
            context, 
            isUpdate = true, 
            id = id, 
            finding, area, patrolId, picId, priorityId, riskLevelId, findingTypeId, categoryId, 
            countermeasureSuggestion, countermeasure, delayReason, 
            imageUris, 
            newImprovedImageUris
        )
    }

    private fun submitFinding(
        context: Context,
        isUpdate: Boolean,
        id: Int,
        finding: String,
        area: String,
        patrolId: Int,
        picId: Int?,
        priorityId: Int?,
        riskLevelId: Int?,
        findingTypeId: Int?,
        categoryId: Int?,
        countermeasureSuggestion: String?,
        countermeasure: String?, 
        delayReason: String?, 
        imageUris: List<Uri>,
        newImprovedImageUris: List<Uri>
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val multipartImages = mutableListOf<MultipartBody.Part>()

                imageUris.forEachIndexed { index, uri ->
                    val file = uriToFile(context, uri)
                    multipartImages.add(
                        MultipartBody.Part.createFormData(
                            name = "findingPictures",
                            filename = "finding_$index.jpg",
                            body = file.asRequestBody("image/*".toMediaType())
                        )
                    )
                }

                newImprovedImageUris.forEachIndexed { index, uri ->
                    val file = uriToFile(context, uri)
                    multipartImages.add(
                        MultipartBody.Part.createFormData(
                            name = "improvedPictures",
                            filename = "improved_$index.jpg",
                            body = file.asRequestBody("image/*".toMediaType())
                        )
                    )
                }

                if (isUpdate) {
                    repository.updateFinding(
                        id = id,
                        finding = finding,
                        area = area,
                        patrolId = patrolId,
                        picId = picId,
                        priorityId = priorityId,
                        riskLevelId = riskLevelId,
                        findingTypeId = findingTypeId,
                        categoryId = categoryId,
                        countermeasureSuggestion = countermeasureSuggestion,
                        countermeasure = countermeasure,
                        delayReason = delayReason,
                        images = multipartImages
                    )
                    _createdFindingId.value = id
                } else {
                    val response = repository.createFinding(
                        finding = finding,
                        area = area,
                        patrolId = patrolId,
                        picId = picId,
                        priorityId = priorityId,
                        riskLevelId = riskLevelId,
                        findingTypeId = findingTypeId,
                        categoryId = categoryId,
                        countermeasureSuggestion = countermeasureSuggestion,
                        countermeasure = countermeasure, // Added missing parameter
                        delayReason = delayReason, // Added missing parameter
                        images = multipartImages
                    )
                    _createdFindingId.value = response.data.findingId
                }

                _success.value = true
                _loading.value = false

            } catch (e: Exception) {
                _loading.value = false
                _error.value = e.message
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("finding_", ".jpg", context.cacheDir)
        tempFile.outputStream().use { output ->
            inputStream?.copyTo(output)
        }
        return tempFile
    }
}
