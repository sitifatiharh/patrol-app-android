package com.rahma.findingpatrol.viewmodel.finding

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahma.findingpatrol.data.repository.FindingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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
        countermeasureSuggestion: String?,
        imageUris: List<Uri>
    ) {
        executeAction(context) {
            val findingPictures = imageUris.map { uri ->
                uriToMultipart(context, uri, "findingPictures")
            }
            val response = repository.createFinding(
                finding, area, patrolId, picId, priorityId, riskLevelId, findingTypeId, countermeasureSuggestion, findingPictures
            )
            _createdFindingId.value = response.data.findingId
        }
    }

    fun patrolEditFinding(
        context: Context,
        id: Int,
        finding: String?,
        area: String?,
        patrolId: Int?,
        picId: Int?,
        priorityId: Int?,
        riskLevelId: Int?,
        findingTypeId: Int?,
        countermeasureSuggestion: String?,
        imageUris: List<Uri>
    ) {
        executeAction(context) {
            val findingPictures = imageUris.map { uri -> uriToMultipart(context, uri, "findingPictures") }
            repository.patrolEditFinding(
                id, finding, area, patrolId, picId, priorityId, riskLevelId, findingTypeId, countermeasureSuggestion, findingPictures
            )
            _createdFindingId.value = id
        }
    }

    fun assignPIC(
        context: Context,
        id: Int,
        picId: Int?,
        patrolId: Int?,
        finding: String?,
        area: String?,
        priorityId: Int?,
        riskLevelId: Int?,
        findingTypeId: Int?,
        countermeasure: String?,
        imageUris: List<Uri>
    ) {
        executeAction(context) {
            val findingPictures = imageUris.map { uri -> uriToMultipart(context, uri, "findingPictures") }
            repository.assignPIC(
                id, picId, patrolId, finding, area, priorityId, riskLevelId, findingTypeId, countermeasure, findingPictures
            )
            _createdFindingId.value = id
        }
    }

    fun updateFindingByPIC(
        context: Context, id: Int, countermeasure: String, delayReason: String, imageUris: List<Uri>
    ) {
        executeAction(context) {
            val improvedPictures = imageUris.map { uri -> uriToMultipart(context, uri, "improvedPictures") }
            repository.updateFindingByPIC(id, countermeasure, delayReason, improvedPictures)
            _createdFindingId.value = id
        }
    }

    fun recheckFinding(
        context: Context, id: Int, categoryId: Int?, picId: Int?, patrolId: Int?,
        finding: String?, area: String?, priorityId: Int?, riskLevelId: Int?, findingTypeId: Int?,
        countermeasure: String?, findingUris: List<Uri>, improvedUris: List<Uri>
    ) {
        executeAction(context) {
            val findingPictures = findingUris.map { uri -> uriToMultipart(context, uri, "findingPictures") }
            val improvedPictures = improvedUris.map { uri -> uriToMultipart(context, uri, "improvedPictures") }
            repository.recheckFinding(
                id, categoryId, picId, patrolId, finding, area, priorityId, riskLevelId,
                findingTypeId, countermeasure, findingPictures, improvedPictures
            )
            _createdFindingId.value = id
        }
    }

    fun generalEditFinding(
        context: Context, id: Int, categoryId: Int?, picId: Int?, patrolId: Int?,
        finding: String?, area: String?, priorityId: Int?, riskLevelId: Int?, findingTypeId: Int?,
        countermeasure: String?, findingUris: List<Uri>, improvedUris: List<Uri>
    ) {
        executeAction(context) {
            val findingPictures = findingUris.map { uri -> uriToMultipart(context, uri, "findingPictures") }
            val improvedPictures = improvedUris.map { uri -> uriToMultipart(context, uri, "improvedPictures") }
            repository.generalEditFinding(
                id, categoryId, picId, patrolId, finding, area, priorityId, riskLevelId,
                findingTypeId, countermeasure, findingPictures, improvedPictures
            )
            _createdFindingId.value = id
        }
    }

    private fun executeAction(context: Context, action: suspend () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                action()
                _success.value = true
            } catch (e: Exception) {
                Log.e("CreateFindingVM", "Action failed", e)
                _error.value = e.message ?: "An unknown error occurred"
            } finally {
                _loading.value = false
            }
        }
    }

    private fun uriToMultipart(context: Context, uri: Uri, partName: String): MultipartBody.Part {
        val file = uriToFile(context, uri)
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestBody)
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Can't open input stream from URI: $uri")
        val fileName = "upload_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)
        file.outputStream().use { output -> inputStream.copyTo(output) }
        inputStream.close()
        return file
    }
}
