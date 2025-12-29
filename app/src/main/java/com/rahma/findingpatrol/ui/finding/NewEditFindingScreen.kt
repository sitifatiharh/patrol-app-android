package com.rahma.findingpatrol.ui.finding

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rahma.findingpatrol.ui.components.DeleteConfirmationDialog
import com.rahma.findingpatrol.ui.components.FormRow
import com.rahma.findingpatrol.ui.components.FullScreenImageViewer
import com.rahma.findingpatrol.ui.components.ImagePreviewGrid
import com.rahma.findingpatrol.ui.components.ImageSourceSelectionDialog
import com.rahma.findingpatrol.ui.components.MetadataDropdown
import com.rahma.findingpatrol.util.fixImageUrl
import com.rahma.findingpatrol.viewmodel.finding.CreateFindingViewModel
import com.rahma.findingpatrol.viewmodel.finding.FindingDetailState
import com.rahma.findingpatrol.viewmodel.finding.FindingDetailViewModel
import com.rahma.findingpatrol.viewmodel.metadata.MetadataState
import com.rahma.findingpatrol.viewmodel.metadata.MetadataViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEditFindingScreen(
    navController: NavController,
    viewModel: CreateFindingViewModel,
    metadataViewModel: MetadataViewModel,
    findingDetailViewModel: FindingDetailViewModel,
    findingId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val context = LocalContext.current
    val isEditMode = findingId != 0
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    var findingText by rememberSaveable { mutableStateOf("") }
    var areaText by rememberSaveable { mutableStateOf("") }
    var countermeasureText by rememberSaveable { mutableStateOf("") }

    var selectedPatrolId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedPicId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedPriorityId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedRiskLevelId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedFindingTypeId by rememberSaveable { mutableStateOf<Int?>(null) }

    var selectedImageUris by rememberSaveable { mutableStateOf<List<Uri>>(emptyList()) }
    var existingImages by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    var patrolExpanded by remember { mutableStateOf(false) }
    var picExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var riskExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Any?>(null) }
    var showImageViewer by remember { mutableStateOf<Pair<List<Any>, Int>?>(null) }

    val metadataState by metadataViewModel.metadataState.collectAsState()
    val metadataData = (metadataState as? MetadataState.Success)?.data
    val patrols = metadataData?.patrol ?: emptyList()
    val pics = metadataData?.pics ?: emptyList()
    val priorities = metadataData?.priority ?: emptyList()
    val risks = metadataData?.riskLevel ?: emptyList()
    val types = metadataData?.findingType ?: emptyList()

    val selectedPatrol = patrols.find { it.patrolId == selectedPatrolId }
    val selectedPic = pics.find { it.sectionId == selectedPicId }
    val selectedPriority = priorities.find { it.priorityId == selectedPriorityId }
    val selectedRiskLevel = risks.find { it.riskLevelId == selectedRiskLevelId }
    val selectedFindingType = types.find { it.findingTypeId == selectedFindingTypeId }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                val currentCount = selectedImageUris.size + existingImages.size
                val availableSlots = 10 - currentCount
                if (availableSlots > 0) {
                    selectedImageUris = (selectedImageUris + uris.take(availableSlots)).distinct()
                } else {
                    Toast.makeText(context, "Maksimal 10 gambar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val newCameraImage = savedStateHandle?.getStateFlow<Uri?>("new_image", null)?.collectAsState()?.value

    LaunchedEffect(newCameraImage) {
        newCameraImage?.let {
            val currentCount = selectedImageUris.size + existingImages.size
            if (currentCount < 10) {
                selectedImageUris = (selectedImageUris + it).distinct()
            } else {
                Toast.makeText(context, "Maksimal 10 gambar", Toast.LENGTH_SHORT).show()
            }
            savedStateHandle.remove<Uri>("new_image")
        }
    }

    LaunchedEffect(Unit) {
        if (!isEditMode) viewModel.resetState()
        metadataViewModel.loadMetadata()
        if (isEditMode) findingDetailViewModel.getFindingDetail(findingId)
    }

    val detailState by findingDetailViewModel.state.collectAsState()
    LaunchedEffect(detailState, metadataState) {
        if (isEditMode && detailState is FindingDetailState.Success && metadataState is MetadataState.Success) {
            val detail = (detailState as FindingDetailState.Success).data
            if (findingText.isEmpty()) findingText = detail.finding
            if (areaText.isEmpty()) areaText = detail.area
            if (countermeasureText.isEmpty()) countermeasureText = detail.countermeasureSuggestion ?: ""
            if (selectedPatrolId == null) selectedPatrolId = patrols.find { it.patrolName == detail.patrolName }?.patrolId
            if (selectedPicId == null) selectedPicId = pics.find { it.sectionName == detail.picName }?.sectionId
            if (selectedPriorityId == null) selectedPriorityId = priorities.find { it.priorityName == detail.priorityName }?.priorityId
            if (selectedRiskLevelId == null) selectedRiskLevelId = risks.find { it.riskLevel == detail.riskLevelName }?.riskLevelId
            if (selectedFindingTypeId == null) selectedFindingTypeId = types.find { it.typeName == detail.findingTypeName }?.findingTypeId
            if (existingImages.isEmpty()) {
                existingImages = detail.images.filter { it.imageType == "finding" }.map { fixImageUrl(it.imageUrl) }
            }
        }
    }

    val loading by viewModel.loading.collectAsState()
    val success by viewModel.success.collectAsState()
    val error by viewModel.error.collectAsState()
    val createdId by viewModel.createdFindingId.collectAsState()

    LaunchedEffect(success) {
        if (success) {
            val message = if (isEditMode) "Laporan berhasil diperbarui!" else "Laporan berhasil dibuat!"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            val targetId = if (isEditMode) findingId else createdId
            if (targetId != null && targetId != 0) {
                onNavigateToDetail(targetId)
            } else {
                onNavigateBack()
            }
            viewModel.resetState()
        }
    }

    LaunchedEffect(error) {
        error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    if (showImageSourceDialog) {
        ImageSourceSelectionDialog(
            onDismiss = { showImageSourceDialog = false },
            onCameraClick = {
                showImageSourceDialog = false
                navController.navigate("camera")
            },
            onGalleryClick = {
                showImageSourceDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }

    if (showDeleteDialog != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                val imageToDelete = showDeleteDialog
                when (imageToDelete) {
                    is Uri -> selectedImageUris = selectedImageUris.filter { it != imageToDelete }
                    is String -> existingImages = existingImages.filter { it != imageToDelete }
                }
                showDeleteDialog = null
                showImageViewer?.let { (currentImages, currentIndex) ->
                    val updatedImages = currentImages.filter { it != imageToDelete }
                    if (updatedImages.isEmpty()) {
                        showImageViewer = null
                    } else {
                        val newIndex = (currentIndex).coerceAtMost(updatedImages.size - 1)
                        showImageViewer = updatedImages to newIndex
                    }
                }
            },
            onDismiss = { showDeleteDialog = null }
        )
    }

    showImageViewer?.let { (images, initialIndex) ->
        FullScreenImageViewer(
            images = images,
            initialIndex = initialIndex,
            onDismiss = { showImageViewer = null },
            onDelete = { image -> showDeleteDialog = image },
            onIndexChanged = { newIndex -> showImageViewer = images to newIndex }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditMode) "Form Edit Temuan" else "Form Finding Patrol", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (metadataState is MetadataState.Loading || (isEditMode && detailState is FindingDetailState.Loading)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                FormRow(
                    label = "Patrol",
                    isMandatory = true
                ) {
                    MetadataDropdown(
                        expanded = patrolExpanded,
                        onExpandedChange = { patrolExpanded = it },
                        selectedOption = selectedPatrol?.patrolName ?: "Pilih Patrol",
                        options = patrols.map { it.patrolName },
                        onOptionSelected = { selected ->
                            selectedPatrolId = patrols.find { it.patrolName == selected }?.patrolId
                            patrolExpanded = false
                        }
                    )
                }

                FormRow(label = "Finding", isMandatory = true) {
                    OutlinedTextField(
                        value = findingText,
                        onValueChange = { findingText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Deskripsi temuan") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.clearFocus() })
                    )
                }

                FormRow(label = "Area", isMandatory = true) {
                    OutlinedTextField(
                        value = areaText,
                        onValueChange = { areaText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Detail lokasi, contoh: Area Boiler A") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.clearFocus() })
                    )
                }

                FormRow(label = "Finding Pictures", isMandatory = true) {
                    ImagePreviewGrid(
                        existingImages = existingImages,
                        selectedImageUris = selectedImageUris,
                        onAddImageClick = { showImageSourceDialog = true },
                        onImageClick = { images, index -> showImageViewer = images to index },
                        onDeleteClick = { showDeleteDialog = it }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                FormRow(label = "PIC") {
                    MetadataDropdown(
                        expanded = picExpanded,
                        onExpandedChange = { picExpanded = it },
                        selectedOption = selectedPic?.sectionName ?: "Pilih PIC",
                        options = pics.map { it.sectionName },
                        onOptionSelected = { selected ->
                            selectedPicId = pics.find { it.sectionName == selected }?.sectionId
                            picExpanded = false
                        },
                        clearSelectionEnabled = true,
                        onClearSelection = { selectedPicId = null }
                    )
                }

                FormRow(label = "Priority") {
                    MetadataDropdown(
                        expanded = priorityExpanded,
                        onExpandedChange = { priorityExpanded = it },
                        selectedOption = selectedPriority?.priorityName ?: "Pilih Prioritas",
                        options = priorities.map { it.priorityName },
                        onOptionSelected = { selected ->
                            selectedPriorityId = priorities.find { it.priorityName == selected }?.priorityId
                            priorityExpanded = false
                        },
                        clearSelectionEnabled = true,
                        onClearSelection = { selectedPriorityId = null }
                    )
                }

                FormRow(label = "Risk Level") {
                    MetadataDropdown(
                        expanded = riskExpanded,
                        onExpandedChange = { riskExpanded = it },
                        selectedOption = selectedRiskLevel?.riskLevel ?: "Pilih Tingkat Risiko",
                        options = risks.map { it.riskLevel },
                        onOptionSelected = { selected ->
                            selectedRiskLevelId = risks.find { it.riskLevel == selected }?.riskLevelId
                            riskExpanded = false
                        },
                        clearSelectionEnabled = true,
                        onClearSelection = { selectedRiskLevelId = null }
                    )
                }

                FormRow(label = "Finding Type") {
                    MetadataDropdown(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = it },
                        selectedOption = selectedFindingType?.typeName ?: "Pilih Tipe Temuan",
                        options = types.map { it.typeName },
                        onOptionSelected = { selected ->
                            selectedFindingTypeId = types.find { it.typeName == selected }?.findingTypeId
                            typeExpanded = false
                        },
                        clearSelectionEnabled = true,
                        onClearSelection = { selectedFindingTypeId = null }
                    )
                }

                FormRow(label = "Countermeasure Suggestion") {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = countermeasureText,
                            onValueChange = { countermeasureText = it },
                            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 120.dp),
                            placeholder = { Text("Usulan tindakan perbaikan") },
                        )
                        IconButton(
                            onClick = { focusManager.clearFocus() },
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Done")
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    val isFormValid = selectedPatrolId != null &&
                            findingText.isNotBlank() &&
                            areaText.isNotBlank() &&
                            (existingImages.isNotEmpty() || selectedImageUris.isNotEmpty())

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (isEditMode) {
                                viewModel.patrolEditFinding(
                                    context = context,
                                    id = findingId,
                                    patrolId = selectedPatrolId,
                                    finding = findingText,
                                    area = areaText,
                                    picId = selectedPicId,
                                    priorityId = selectedPriorityId,
                                    riskLevelId = selectedRiskLevelId,
                                    findingTypeId = selectedFindingTypeId,
                                    countermeasureSuggestion = if (countermeasureText.isNotBlank()) countermeasureText else null,
                                    imageUris = selectedImageUris
                                )
                            } else {
                                selectedPatrolId?.let {
                                    viewModel.createFinding(
                                        context = context,
                                        patrolId = it,
                                        finding = findingText,
                                        area = areaText,
                                        picId = selectedPicId,
                                        priorityId = selectedPriorityId,
                                        riskLevelId = selectedRiskLevelId,
                                        findingTypeId = selectedFindingTypeId,
                                        countermeasureSuggestion = if (countermeasureText.isNotBlank()) countermeasureText else null,
                                        imageUris = selectedImageUris
                                    )
                                }
                            }
                        },
                        enabled = isFormValid && !loading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text(if (isEditMode) "Edit Temuan" else "Submit")
                        }
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}
