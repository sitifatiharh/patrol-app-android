package com.rahma.findingpatrol.ui.finding

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rahma.findingpatrol.ui.components.DeleteConfirmationDialog
import com.rahma.findingpatrol.ui.components.FullScreenImageViewer
import com.rahma.findingpatrol.ui.components.ImageSourceSelectionDialog
import com.rahma.findingpatrol.ui.metadata.FormImagePicker
import com.rahma.findingpatrol.ui.metadata.FormRow
import com.rahma.findingpatrol.ui.metadata.FormSection
import com.rahma.findingpatrol.ui.metadata.MetadataDropdown
import com.rahma.findingpatrol.util.fixImageUrl
import com.rahma.findingpatrol.viewmodel.finding.CreateFindingViewModel
import com.rahma.findingpatrol.viewmodel.finding.FindingDetailState
import com.rahma.findingpatrol.viewmodel.finding.FindingDetailViewModel
import com.rahma.findingpatrol.viewmodel.metadata.MetadataState
import com.rahma.findingpatrol.viewmodel.metadata.MetadataViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ImprovementActionScreen(
    navController: NavController,
    viewModel: CreateFindingViewModel,
    metadataViewModel: MetadataViewModel,
    findingDetailViewModel: FindingDetailViewModel,
    onNavigateBack: () -> Unit,
    findingId: Int
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // --- State Variables (using rememberSaveable) ---
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
    var existingFindingImages by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var newImprovedImages by rememberSaveable { mutableStateOf<List<Uri>>(emptyList()) }
    var existingImprovedImages by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    // --- UI State ---
    var showImageSourceDialog by remember { mutableStateOf<String?>(null) } // "finding" or "improved"
    var showDeleteDialog by remember { mutableStateOf<Pair<String, Any>?>(null) } // Pair of type and image
    var showImageViewer by remember { mutableStateOf<Pair<String, Int>?>(null) } // Pair of type and index

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

    // Pre-fill
    var statusName by remember { mutableStateOf("") }

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
            if (countermeasureText.isEmpty()) countermeasureText =
                detail.countermeasureSuggestion ?: ""

            if (selectedPatrolId == null) selectedPatrolId =
                patrols.find { it.patrolName == detail.patrolName }?.patrolId
            if (selectedPicId == null) selectedPicId =
                pics.find { it.sectionName == detail.picName }?.sectionId
            if (selectedPriorityId == null) selectedPriorityId =
                priorities.find { it.priorityName == detail.priorityName }?.priorityId
            if (selectedRiskLevelId == null) selectedRiskLevelId =
                risks.find { it.riskLevel == detail.riskLevelName }?.riskLevelId
            if (selectedFindingTypeId == null) selectedFindingTypeId =
                types.find { it.typeName == detail.findingTypeName }?.findingTypeId
            if (selectedCategoryId == null) selectedCategoryId =
                categories.find { it.categoryName == detail.categoryName }?.categoryId

            if (existingFindingImages.isEmpty()) {
                existingFindingImages =
                    detail.images.filter { it.imageType == "finding" }.map { fixImageUrl(it.imageUrl) }
            }

            if (existingImprovedImages.isEmpty()) {
                existingImprovedImages =
                    detail.images.filter { it.imageType == "improved" }.map { fixImageUrl(it.imageUrl) }
            }
        }
    }

    // --- Image Launchers & Handlers ---
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                val target = showImageSourceDialog
                val currentUris = if (target == "finding") newFindingImages else newImprovedImages
                val updatedUris = (currentUris + uris).distinct()
                if (target == "finding") newFindingImages = updatedUris else newImprovedImages =
                    updatedUris
                showImageSourceDialog = null
            }
        }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val newCameraImage = savedStateHandle?.getStateFlow<Uri?>("new_image", null)?.collectAsState()?.value

    LaunchedEffect(newCameraImage) {
        newCameraImage?.let {
            val target = showImageSourceDialog
            val currentUris = if (target == "finding") newFindingImages else newImprovedImages
            val updatedUris = (currentUris + it).distinct()
            if (target == "finding") newFindingImages = updatedUris else newImprovedImages =
                updatedUris
            savedStateHandle.remove<Uri>("new_image")
            showImageSourceDialog = null
        }
    }

    // --- Dialogs ---
    if (showImageSourceDialog != null) {
        ImageSourceSelectionDialog(
            onDismiss = { showImageSourceDialog = null },
            onCameraClick = {
                navController.navigate("camera")
            },
            onGalleryClick = {
                galleryLauncher.launch("image/*")
            }
        )
    }

    if (showDeleteDialog != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                val (type, image) = showDeleteDialog!!
                if (type == "finding") {
                    if (image is Uri) newFindingImages =
                        newFindingImages - image else existingFindingImages =
                        existingFindingImages - (image as String)
                } else {
                    if (image is Uri) newImprovedImages =
                        newImprovedImages - image else existingImprovedImages =
                        existingImprovedImages - (image as String)
                }
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }

    if (showImageViewer != null) {
        val (type, index) = showImageViewer!!
        val allImages =
            if (type == "finding") (existingFindingImages + newFindingImages) else (existingImprovedImages + newImprovedImages)
        FullScreenImageViewer(
            images = allImages.map<Any, Any> { it },
            initialIndex = index,
            onDismiss = { showImageViewer = null },
            onDelete = { image -> showDeleteDialog = Pair(type, image) }
        )
    }

    // Logic Enable/Disable
    val isOpen = statusName == "OPEN"
    val isReady = statusName == "READY FOR RECHECK"
    val isClosed = statusName == "CLOSED"

    val canEditDetails = isOpen
    val canEditCategory = isReady || isClosed

    // Submit Logic
    val loading by viewModel.loading.collectAsState()
    val success by viewModel.success.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(success) {
        if (success) {
            Toast.makeText(context, "Aksi berhasil disimpan!", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }
    LaunchedEffect(error) { error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Improvement Action ($statusName)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.Close,
                            "Close"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {

            // Finding Details
            FormSection(title = "Finding Details") {
                FormRow("Finding", {
                    OutlinedTextField(
                        value = findingText,
                        onValueChange = { if (canEditDetails) findingText = it },
                        readOnly = !canEditDetails,
                        modifier = Modifier.fillMaxWidth()
                    )
                })
                FormRow("Area", {
                    OutlinedTextField(
                        value = areaText,
                        onValueChange = { if (canEditDetails) areaText = it },
                        readOnly = !canEditDetails,
                        modifier = Modifier.fillMaxWidth()
                    )
                })
                FormRow("Patrol", {
                    MetadataDropdown(
                        label = "Select Patrol",
                        options = patrols,
                        selectedOption = patrols.find { it.patrolId == selectedPatrolId },
                        onOptionSelected = { selectedPatrolId = it.patrolId },
                        itemLabel = { it.patrolName },
                        expanded = patrolExpanded,
                        onExpandedChange = { patrolExpanded = it },
                        enabled = canEditDetails
                    )
                })
                FormImagePicker(
                    label = "Finding Photos",
                    selectedImageUris = newFindingImages,
                    existingImages = existingFindingImages,
                    onPickImage = { showImageSourceDialog = "finding" },
                    onRemoveImage = { uri -> showDeleteDialog = Pair("finding", uri) },
                    onRemoveExisting = { url -> showDeleteDialog = Pair("finding", url) },
                    placeholderText = "Add finding photos",
                    enabled = canEditDetails
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Assignment & Metadata
            FormSection(title = "Assignment & Metadata") {
                FormRow("PIC" + if (isOpen) " *" else "", {
                    MetadataDropdown(
                        label = "Select PIC",
                        options = pics,
                        selectedOption = pics.find { it.sectionId == selectedPicId },
                        onOptionSelected = { selectedPicId = it.sectionId },
                        itemLabel = { it.sectionName },
                        expanded = picExpanded,
                        onExpandedChange = { picExpanded = it },
                        enabled = !isClosed
                    )
                })
                FormRow("Priority", {
                    MetadataDropdown(
                        label = "Select Priority",
                        options = priorities,
                        selectedOption = priorities.find { it.priorityId == selectedPriorityId },
                        onOptionSelected = { selectedPriorityId = it.priorityId },
                        itemLabel = { it.priorityName },
                        expanded = priorityExpanded,
                        onExpandedChange = { priorityExpanded = it },
                        enabled = !isClosed
                    )
                })
                FormRow("Risk Level", {
                    MetadataDropdown(
                        label = "Select Risk",
                        options = risks,
                        selectedOption = risks.find { it.riskLevelId == selectedRiskLevelId },
                        onOptionSelected = { selectedRiskLevelId = it.riskLevelId },
                        itemLabel = { it.riskLevel },
                        expanded = riskExpanded,
                        onExpandedChange = { riskExpanded = it },
                        enabled = !isClosed
                    )
                })
                FormRow("Type", {
                    MetadataDropdown(
                        label = "Select Type",
                        options = types,
                        selectedOption = types.find { it.findingTypeId == selectedFindingTypeId },
                        onOptionSelected = { selectedFindingTypeId = it.findingTypeId },
                        itemLabel = { it.typeName },
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = it },
                        enabled = !isClosed
                    )
                })
                FormRow("Suggestion", {
                    OutlinedTextField(
                        value = countermeasureText,
                        onValueChange = { countermeasureText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        readOnly = isClosed
                    )
                })
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Improvement Photos
            FormSection(title = "Improvement Photos") {
                FormImagePicker(
                    label = "Improved Photos",
                    selectedImageUris = newImprovedImages,
                    existingImages = existingImprovedImages,
                    onPickImage = { showImageSourceDialog = "improved" },
                    onRemoveImage = { uri -> showDeleteDialog = Pair("improved", uri) },
                    onRemoveExisting = { url -> showDeleteDialog = Pair("improved", url) },
                    placeholderText = "Add improved photos",
                    enabled = !isClosed
                )
            }

            // Closing
            if (isReady || isClosed) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                FormSection("Closing") {
                    FormRow(
                        "Category" + if (isReady) " *" else "",
                        {
                            MetadataDropdown(
                                label = "Select Category",
                                options = categories,
                                selectedOption = categories.find { it.categoryId == selectedCategoryId },
                                onOptionSelected = { selectedCategoryId = it.categoryId },
                                itemLabel = { it.categoryName },
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = it },
                                enabled = canEditCategory
                            )
                        })
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
