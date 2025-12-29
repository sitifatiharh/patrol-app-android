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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rahma.findingpatrol.ui.components.CancelButton
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
fun ImprovementActionScreen(
    navController: NavController,
    viewModel: CreateFindingViewModel,
    metadataViewModel: MetadataViewModel,
    findingDetailViewModel: FindingDetailViewModel,
    findingId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    // --- State Variables ---
    var findingText by rememberSaveable { mutableStateOf("") }
    var areaText by rememberSaveable { mutableStateOf("") }
    var countermeasureText by rememberSaveable { mutableStateOf("") }

    var selectedPatrolId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedPicId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedPriorityId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedRiskLevelId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedFindingTypeId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedCategoryId by rememberSaveable { mutableStateOf<Int?>(null) }

    // --- Image States ---
    var newFindingImages by rememberSaveable { mutableStateOf<List<Uri>>(emptyList()) }
    var newImprovedImages by rememberSaveable { mutableStateOf<List<Uri>>(emptyList()) }
    var existingFindingImages by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var existingImprovedImages by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    // --- UI State ---
    var imagePickerTarget by remember { mutableStateOf<String?>(null) } // "finding" or "improved"
    var showDeleteDialog by remember { mutableStateOf<Any?>(null) }
    var showImageViewer by remember { mutableStateOf<Pair<List<Any>, Int>?>(null) }

    // Dropdown Expanded
    var patrolExpanded by remember { mutableStateOf(false) }
    var picExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var riskExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // Load Data
    val metadataState by metadataViewModel.metadataState.collectAsState()
    val detailState by findingDetailViewModel.state.collectAsState()

    val metadataData = (metadataState as? MetadataState.Success)?.data
    val patrols = metadataData?.patrol ?: emptyList()
    val pics = metadataData?.pics ?: emptyList()
    val priorities = metadataData?.priority ?: emptyList()
    val risks = metadataData?.riskLevel ?: emptyList()
    val types = metadataData?.findingType ?: emptyList()
    val categories = metadataData?.category ?: emptyList()

    val selectedPatrol = patrols.find { it.patrolId == selectedPatrolId }
    val selectedPic = pics.find { it.sectionId == selectedPicId }
    val selectedPriority = priorities.find { it.priorityId == selectedPriorityId }
    val selectedRiskLevel = risks.find { it.riskLevelId == selectedRiskLevelId }
    val selectedFindingType = types.find { it.findingTypeId == selectedFindingTypeId }
    val selectedCategory = categories.find { it.categoryId == selectedCategoryId }

    // --- Status Logic ---
    var statusName by remember { mutableStateOf("") }
    val isOpen = statusName == "OPEN"
    val isAssigned = statusName == "ASSIGNED"
    val isReady = statusName == "READY FOR RECHECK"
    val isClosed = statusName == "CLOSED"

    val closingSectionIsVisible = isReady || isClosed

    LaunchedEffect(Unit) {
        metadataViewModel.loadMetadata()
        viewModel.resetState()
        if (findingId > 0) {
            findingDetailViewModel.getFindingDetail(findingId)
        }
    }

    LaunchedEffect(detailState, metadataData) {
        if (detailState is FindingDetailState.Success && metadataData != null) {
            val detail = (detailState as FindingDetailState.Success).data
            statusName = detail.statusName.uppercase()

            if (findingText.isEmpty()) findingText = detail.finding
            if (areaText.isEmpty()) areaText = detail.area

            if (countermeasureText.isEmpty()) {
                countermeasureText = if (isOpen || isAssigned) {
                    detail.countermeasureSuggestion ?: ""
                } else {
                    detail.countermeasure ?: ""
                }
            }

            if (selectedPatrolId == null) selectedPatrolId = patrols.find { it.patrolName == detail.patrolName }?.patrolId
            if (selectedPicId == null) selectedPicId = pics.find { it.sectionName == detail.picName }?.sectionId
            if (selectedPriorityId == null) selectedPriorityId = priorities.find { it.priorityName == detail.priorityName }?.priorityId
            if (selectedRiskLevelId == null) selectedRiskLevelId = risks.find { it.riskLevel == detail.riskLevelName }?.riskLevelId
            if (selectedFindingTypeId == null) selectedFindingTypeId = types.find { it.typeName == detail.findingTypeName }?.findingTypeId
            if (selectedCategoryId == null) selectedCategoryId = categories.find { it.categoryName == detail.categoryName }?.categoryId

            if (existingFindingImages.isEmpty()) {
                existingFindingImages = detail.images.filter { it.imageType == "finding" }.map { fixImageUrl(it.imageUrl) }
            }
            if (existingImprovedImages.isEmpty()) {
                existingImprovedImages = detail.images.filter { it.imageType == "improved" }.map { fixImageUrl(it.imageUrl) }
            }
        }
    }

    // --- Image Launchers & Handlers ---
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            if (imagePickerTarget == "finding") {
                val currentCount = newFindingImages.size + existingFindingImages.size
                val availableSlots = 10 - currentCount
                if (availableSlots > 0) {
                    newFindingImages = (uris.take(availableSlots) + newFindingImages).distinct()
                } else {
                    Toast.makeText(context, "Maksimal 10 gambar", Toast.LENGTH_SHORT).show()
                }
            } else { // improved
                val currentCount = newImprovedImages.size + existingImprovedImages.size
                val availableSlots = 10 - currentCount
                if (availableSlots > 0) {
                    newImprovedImages = (uris.take(availableSlots) + newImprovedImages).distinct()
                } else {
                    Toast.makeText(context, "Maksimal 10 gambar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val newCameraImage = savedStateHandle?.getStateFlow<Uri?>("new_image", null)?.collectAsState()?.value

    LaunchedEffect(newCameraImage) {
        newCameraImage?.let { uri ->
            imagePickerTarget?.let { target ->
                if (target == "finding") {
                    if (newFindingImages.size + existingFindingImages.size < 10) {
                        newFindingImages = (listOf(uri) + newFindingImages).distinct()
                    } else {
                        Toast.makeText(context, "Maksimal 10 gambar", Toast.LENGTH_SHORT).show()
                    }
                } else { // improved
                    if (newImprovedImages.size + existingImprovedImages.size < 10) {
                        newImprovedImages = (listOf(uri) + newImprovedImages).distinct()
                    } else {
                        Toast.makeText(context, "Maksimal 10 gambar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            savedStateHandle.remove<Uri>("new_image")
            imagePickerTarget = null // reset target
        }
    }

    // --- Dialogs ---
    if (imagePickerTarget != null) {
        ImageSourceSelectionDialog(
            onDismiss = { imagePickerTarget = null },
            onCameraClick = { navController.navigate("camera") },
            onGalleryClick = { galleryLauncher.launch("image/*") }
        )
    }

    if (showDeleteDialog != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                val imageToDelete = showDeleteDialog
                when (imageToDelete) {
                    is Uri -> {
                        newFindingImages = newFindingImages.filter { it != imageToDelete }
                        newImprovedImages = newImprovedImages.filter { it != imageToDelete }
                    }

                    is String -> {
                        existingFindingImages = existingFindingImages.filter { it != imageToDelete }
                        existingImprovedImages = existingImprovedImages.filter { it != imageToDelete }
                    }
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

    // --- Submit Logic & UI State ---
    val loading by viewModel.loading.collectAsState()
    val success by viewModel.success.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(success) {
        if (success) {
            Toast.makeText(context, "Aksi berhasil disimpan!", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }
    LaunchedEffect(error) { error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() } }

    val title = when {
        isOpen -> "Form Penugasan PIC"
        isAssigned -> "Form Edit Penugasan"
        isReady -> "Form Tutup Temuan"
        isClosed -> "Form Edit Temuan"
        else -> "Loading..."
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, maxLines = 1, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (detailState is FindingDetailState.Loading || (statusName.isEmpty() && findingId > 0)) {
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
                if (isOpen || isAssigned || isReady) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                            append(" WAJIB DIISI")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                FormRow(label = "Finding") { OutlinedTextField(value = findingText, onValueChange = {}, modifier = Modifier.fillMaxWidth(), readOnly = true) }
                FormRow(label = "Area") { OutlinedTextField(value = areaText, onValueChange = {}, modifier = Modifier.fillMaxWidth(), readOnly = true) }
                FormRow(label = "Patrol") {
                    MetadataDropdown(expanded = patrolExpanded, onExpandedChange = { patrolExpanded = it }, selectedOption = selectedPatrol?.patrolName ?: "Pilih Patrol", options = patrols.map { it.patrolName }, onOptionSelected = { selected -> selectedPatrolId = patrols.find { it.patrolName == selected }?.patrolId }, readOnly = true)
                }

                FormRow(label = "Finding Pictures") {
                    ImagePreviewGrid(
                        existingImages = existingFindingImages,
                        selectedImageUris = newFindingImages,
                        onAddImageClick = { imagePickerTarget = "finding" },
                        onImageClick = { _, index -> showImageViewer = (newFindingImages + existingFindingImages) to index },
                        onDeleteClick = { image -> showDeleteDialog = image }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                FormRow(label = "PIC", isMandatory = isOpen || isAssigned) {
                    MetadataDropdown(expanded = picExpanded, onExpandedChange = { picExpanded = it }, selectedOption = selectedPic?.sectionName ?: "Pilih PIC", options = pics.map { it.sectionName }, onOptionSelected = { selected -> selectedPicId = pics.find { it.sectionName == selected }?.sectionId ; picExpanded = false }, readOnly = isReady || isClosed)
                }
                FormRow(label = "Priority") {
                    MetadataDropdown(expanded = priorityExpanded, onExpandedChange = { priorityExpanded = it }, selectedOption = selectedPriority?.priorityName ?: "Pilih Prioritas", options = priorities.map { it.priorityName }, onOptionSelected = { selected -> selectedPriorityId = priorities.find { it.priorityName == selected }?.priorityId ; priorityExpanded = false })
                }
                FormRow(label = "Risk Level") {
                    MetadataDropdown(expanded = riskExpanded, onExpandedChange = { riskExpanded = it }, selectedOption = selectedRiskLevel?.riskLevel ?: "Pilih Tingkat Risiko", options = risks.map { it.riskLevel }, onOptionSelected = { selected -> selectedRiskLevelId = risks.find { it.riskLevel == selected }?.riskLevelId ; riskExpanded = false })
                }
                FormRow(label = "Finding Type") {
                    MetadataDropdown(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }, selectedOption = selectedFindingType?.typeName ?: "Pilih Tipe Temuan", options = types.map { it.typeName }, onOptionSelected = { selected -> selectedFindingTypeId = types.find { it.typeName == selected }?.findingTypeId ; typeExpanded = false })
                }
                FormRow(label = if (isOpen || isAssigned) "Countermeasure Suggestion" else "Countermeasure") {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = countermeasureText, onValueChange = { countermeasureText = it }, modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 80.dp))
                        IconButton(onClick = { focusManager.clearFocus() }, modifier = Modifier.align(Alignment.BottomEnd)) {
                            Icon(Icons.Default.Check, "Done")
                        }
                    }
                }

                if (closingSectionIsVisible) {
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    FormRow(label = "Improved Pictures", isMandatory = isReady) {
                        ImagePreviewGrid(
                            existingImages = existingImprovedImages,
                            selectedImageUris = newImprovedImages,
                            onAddImageClick = { imagePickerTarget = "improved" },
                            onImageClick = { _, index -> showImageViewer = (newImprovedImages + existingImprovedImages) to index },
                            onDeleteClick = { image -> showDeleteDialog = image }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FormRow("Category", isMandatory = isReady) {
                        MetadataDropdown(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }, selectedOption = selectedCategory?.categoryName ?: "Pilih Kategori", options = categories.map { it.categoryName }, onOptionSelected = { selected -> selectedCategoryId = categories.find { it.categoryName == selected }?.categoryId ; categoryExpanded = false })
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CancelButton(onClick = onNavigateBack, modifier = Modifier.weight(1f).height(48.dp))

                    Button(
                        onClick = {
                            if ((isOpen || isAssigned) && selectedPicId == null) {
                                Toast.makeText(context, "Harap pilih PIC", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (isReady && newImprovedImages.isEmpty() && existingImprovedImages.isEmpty()) {
                                Toast.makeText(context, "Harap tambahkan gambar perbaikan", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (isReady && selectedCategoryId == null) {
                                Toast.makeText(context, "Harap pilih Kategori", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            when {
                                isOpen || isAssigned -> viewModel.assignPIC(context = context, id = findingId, picId = selectedPicId, patrolId = selectedPatrolId, finding = findingText, area = areaText, priorityId = selectedPriorityId, riskLevelId = selectedRiskLevelId, findingTypeId = selectedFindingTypeId, countermeasure = countermeasureText, imageUris = newFindingImages)
                                isReady -> viewModel.recheckFinding(context = context, id = findingId, categoryId = selectedCategoryId, picId = selectedPicId, patrolId = selectedPatrolId, finding = findingText, area = areaText, priorityId = selectedPriorityId, riskLevelId = selectedRiskLevelId, findingTypeId = selectedFindingTypeId, countermeasure = countermeasureText, findingUris = newFindingImages, improvedUris = newImprovedImages)
                                isClosed -> viewModel.generalEditFinding(context = context, id = findingId, categoryId = selectedCategoryId, picId = selectedPicId, patrolId = selectedPatrolId, finding = findingText, area = areaText, priorityId = selectedPriorityId, riskLevelId = selectedRiskLevelId, findingTypeId = selectedFindingTypeId, countermeasure = countermeasureText, findingUris = newFindingImages, improvedUris = newImprovedImages)
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = !loading
                    ) {
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            val buttonText = when {
                                isOpen -> "Tugaskan PIC"
                                isAssigned -> "Edit Penugasan"
                                isReady -> "Tutup Temuan"
                                isClosed -> "Edit Temuan"
                                else -> "Simpan"
                            }
                            Text(buttonText, fontSize = 15.sp, maxLines = 1)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
