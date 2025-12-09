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
import okhttp3.RequestBody.Companion.toRequestBody
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

    fun resetState() {
        _success.value = false
        _loading.value = false
        _error.value = null
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
        submitFinding(context, isUpdate = false, id = 0, finding, area, patrolId, picId, priorityId, riskLevelId, findingTypeId, categoryId, countermeasureSuggestion, null, null, imageUris)
    }

    fun updateFinding(
        context: Context,
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
        countermeasure: String? = null, // Added
        delayReason: String? = null, // Added
        imageUris: List<Uri>
    ) {
        submitFinding(context, isUpdate = true, id = id, finding, area, patrolId, picId, priorityId, riskLevelId, findingTypeId, categoryId, countermeasureSuggestion, countermeasure, delayReason, imageUris)
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
        countermeasure: String?, // Added
        delayReason: String?, // Added
        imageUris: List<Uri>
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val mediaType = "text/plain".toMediaType()

                val rbFinding = finding.toRequestBody(mediaType)
                val rbArea = area.toRequestBody(mediaType)
                val rbPatrolId = patrolId.toString().toRequestBody(mediaType)
                val rbPicId = picId?.toString()?.toRequestBody(mediaType)
                val rbPriorityId = priorityId?.toString()?.toRequestBody(mediaType)
                val rbRiskLevelId = riskLevelId?.toString()?.toRequestBody(mediaType)
                val rbFindingTypeId = findingTypeId?.toString()?.toRequestBody(mediaType)
                val rbCategoryId = categoryId?.toString()?.toRequestBody(mediaType)
                val rbCounterSuggestion = countermeasureSuggestion?.toRequestBody(mediaType)
                val rbCountermeasure = countermeasure?.toRequestBody(mediaType)
                val rbDelayReason = delayReason?.toRequestBody(mediaType)

                // Convert images
                val multipartImages = imageUris.mapIndexed { index, uri ->
                    val file = uriToFile(context, uri)
                    MultipartBody.Part.createFormData(
                        name = "files",
                        filename = "image_$index.jpg",
                        body = file.asRequestBody("image/*".toMediaType())
                    )
                }

                if (isUpdate) {
                    repository.updateFinding(
                        id = id,
                        finding = rbFinding,
                        area = rbArea,
                        patrolId = rbPatrolId,
                        picId = rbPicId,
                        priorityId = rbPriorityId,
                        riskLevelId = rbRiskLevelId,
                        findingTypeId = rbFindingTypeId,
                        categoryId = rbCategoryId,
                        countermeasureSuggestion = rbCounterSuggestion,
                        countermeasure = rbCountermeasure,
                        delayReason = rbDelayReason,
                        images = multipartImages
                    )
                } else {
                    repository.createFinding(
                        finding = rbFinding,
                        area = rbArea,
                        patrolId = rbPatrolId,
                        picId = rbPicId,
                        priorityId = rbPriorityId,
                        riskLevelId = rbRiskLevelId,
                        findingTypeId = rbFindingTypeId,
                        categoryId = rbCategoryId,
                        countermeasureSuggestion = rbCounterSuggestion,
                        images = multipartImages
                    )
                }

                _success.value = true
                _loading.value = false

            } catch (e: Exception) {
                _loading.value = false
                _error.value = e.message
            }
        }
    }

    /** Convert Uri -> File (untuk Retrofit multipart upload) */
    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("finding_", ".jpg", context.cacheDir)

        tempFile.outputStream().use { output ->
            inputStream?.copyTo(output)
        }

        return tempFile
    }
}
