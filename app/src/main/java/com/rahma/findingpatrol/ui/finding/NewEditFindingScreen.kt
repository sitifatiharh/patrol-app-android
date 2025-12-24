package com.rahma.findingpatrol.ui.finding

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rahma.findingpatrol.ui.metadata.FormRow
import com.rahma.findingpatrol.ui.metadata.MetadataDropdown
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
    var selectedImageUris by rememberSaveable { mutableStateOf<List<Uri>>(emptyList()) }
    var existingImages by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    // --- UI State ---
    var patrolExpanded by remember { mutableStateOf(false) }
    var picExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var riskExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Any?>(null) }
    var showImageViewer by remember { mutableStateOf<Pair<List<Any>, Int>?>(null) }


    // --- Metadata & Pre-fill Logic ---
    val metadataState by metadataViewModel.metadataState.collectAsState()
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

    // --- Image Launchers ---
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

    // --- Data Loading & Pre-filling ---
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
            if (selectedCategoryId == null) selectedCategoryId = categories.find { it.categoryName == detail.categoryName }?.categoryId
            if (existingImages.isEmpty()) {
                existingImages = detail.images.filter { it.imageType == "finding" }.map { fixImageUrl(it.imageUrl) }
            }
        }
    }

    // --- Submission Logic & Side Effects ---
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
        error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    // --- Dialogs ---
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
                when (val image = showDeleteDialog) {
                    is Uri -> selectedImageUris = selectedImageUris - image
                    is String -> existingImages = existingImages - image
                }
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }

    showImageViewer?.let { (images, initialIndex) ->
        FullScreenImageViewer(
            images = images,
            initialIndex = initialIndex,
            onDismiss = { showImageViewer = null },
            onDelete = { image ->
                showDeleteDialog = image
                // To reflect deletion in the viewer immediately, we also need to update the list
                val updatedImages = images.filter { it != image }
                if (updatedImages.isEmpty()) {
                    showImageViewer = null
                } else {
                    showImageViewer = updatedImages to 0 // Reset to first image
                }
            }
        )
    }


    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditMode) "Edit Temuan" else "Buat Temuan Baru") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text("Batal", fontSize = 19.sp)
                }
                Button(
                    onClick = {
                        val cleanFinding = findingText.trim()
                        val cleanArea = areaText.trim()
                        val cleanCountermeasure = countermeasureText.trim().replace(Regex("\n+"), "\n")

                        if (cleanFinding.isBlank() || cleanArea.isBlank() || selectedPatrolId == null || (selectedImageUris.isEmpty() && existingImages.isEmpty())) {
                            Toast.makeText(context, "Harap isi semua kolom bertanda *", Toast.LENGTH_SHORT).show()
                        } else {
                            if (isEditMode) {
                                viewModel.updateFinding(
                                    context = context, id = findingId, finding = cleanFinding, area = cleanArea, patrolId = selectedPatrolId!!, picId = selectedPicId, priorityId = selectedPriorityId,
                                    riskLevelId = selectedRiskLevelId, findingTypeId = selectedFindingTypeId, categoryId = selectedCategoryId, countermeasureSuggestion = cleanCountermeasure, imageUris = selectedImageUris
                                )
                            } else {
                                viewModel.createFinding(
                                    context = context, finding = cleanFinding, area = cleanArea, patrolId = selectedPatrolId!!, picId = selectedPicId, priorityId = selectedPriorityId,
                                    riskLevelId = selectedRiskLevelId, findingTypeId = selectedFindingTypeId, categoryId = selectedCategoryId, countermeasureSuggestion = cleanCountermeasure, imageUris = selectedImageUris
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD7E8CD), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    enabled = !loading
                ) {
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text(if (isEditMode) "Update" else "Submit", fontSize = 19.sp)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isEditMode) "Edit Finding Patrol" else "Form Finding Patrol",
                fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            val mandatory = buildAnnotatedString {
                append("*")
                addStyle(style = SpanStyle(color = Color.Red), start = 0, end = 1)
                append(" WAJIB DIISI")
            }
            Text(text = mandatory, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            FormRow("Patrol *", { MetadataDropdown("Pilih Patrol", patrols, selectedPatrol, { selectedPatrolId = it.patrolId }, { it.patrolName }, patrolExpanded, { patrolExpanded = it }) })
            FormRow("Finding *", { OutlinedTextField(value = findingText, onValueChange = { findingText = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Deskripsi temuan...", fontSize = 17.sp) }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Gray, focusedBorderColor = Color.Black), textStyle = TextStyle(fontSize = 17.sp)) })
            FormRow("Area *", { OutlinedTextField(value = areaText, onValueChange = { areaText = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Lokasi area...", fontSize = 17.sp) }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Gray, focusedBorderColor = Color.Black), textStyle = TextStyle(fontSize = 17.sp)) })

            FindingFormImagePicker(
                label = "Finding Pictures *",
                imageUris = selectedImageUris,
                existingImageUrls = existingImages,
                onAddImageClick = { showImageSourceDialog = true },
                onRemoveUri = { uri -> showDeleteDialog = uri },
                onRemoveUrl = { url -> showDeleteDialog = url },
                onImageClick = { allImages, index -> showImageViewer = allImages to index }
            )

            FormRow("Countermeasure Suggestion", {
                val keyboardController = LocalSoftwareKeyboardController.current
                val focusManager = LocalFocusManager.current
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = countermeasureText, onValueChange = { countermeasureText = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Saran perbaikan...", fontSize = 17.sp) },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default), keyboardActions = KeyboardActions.Default,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Gray, focusedBorderColor = Color.Black), textStyle = TextStyle(fontSize = 17.sp)
                    )
                    IconButton(onClick = { keyboardController?.hide(); focusManager.clearFocus() }, modifier = Modifier.align(Alignment.BottomEnd)) {
                        Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.Gray)
                    }
                }
            })

            FormRow("PIC", { MetadataDropdown("Pilih PIC", pics, selectedPic, { selectedPicId = it.sectionId }, { it.sectionName }, picExpanded, { picExpanded = it }) })
            FormRow("Priority", { MetadataDropdown("Pilih Prioritas", priorities, selectedPriority, { selectedPriorityId = it.priorityId }, { it.priorityName }, priorityExpanded, { priorityExpanded = it }) })
            FormRow("Risk Level", { MetadataDropdown("Pilih Risiko", risks, selectedRiskLevel, { selectedRiskLevelId = it.riskLevelId }, { it.riskLevel }, riskExpanded, { riskExpanded = it }) })
            FormRow("Type", { MetadataDropdown("Pilih Tipe", types, selectedFindingType, { selectedFindingTypeId = it.findingTypeId }, { it.typeName }, typeExpanded, { typeExpanded = it }) })

            val showCategory = selectedFindingType?.typeName?.equals("Abnormal", ignoreCase = true) == true
            if (showCategory) {
                FormRow("Category", { MetadataDropdown("Pilih Kategori", categories, selectedCategory, { selectedCategoryId = it.categoryId }, { it.categoryName }, categoryExpanded, { categoryExpanded = it }) })
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FindingFormImagePicker(
    label: String,
    imageUris: List<Uri>,
    existingImageUrls: List<String>,
    onAddImageClick: () -> Unit,
    onRemoveUri: (Uri) -> Unit,
    onRemoveUrl: (String) -> Unit,
    onImageClick: (List<Any>, Int) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        val annotatedLabel = buildAnnotatedString {
            append(label)
            val starIndex = label.indexOf('*')
            if (starIndex != -1) {
                addStyle(
                    style = SpanStyle(color = Color.Red),
                    start = starIndex,
                    end = starIndex + 1
                )
            }
        }
        Text(text = annotatedLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))

        val allImages = existingImageUrls.map<String, Any> { it } + imageUris.map<Uri, Any> { it }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (allImages.size < 10) {
                item {
                    AddImageButton(onClick = onAddImageClick)
                }
            }
            itemsIndexed(allImages) { index, image ->
                Box {
                    AsyncImage(
                        model = image,
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onImageClick(allImages, index) },
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = {
                            when (image) {
                                is Uri -> onRemoveUri(image)
                                is String -> onRemoveUrl(image)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove Image", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AddImageButton(onClick: () -> Unit) {
    val stroke = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = Color.Gray,
                style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AddAPhoto, contentDescription = "Add Image", tint = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Tambah", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ImageSourceSelectionDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Sumber Gambar") },
        text = { Text("Ambil gambar dari kamera atau pilih dari galeri.") },
        confirmButton = {
            TextButton(onClick = onCameraClick) { Text("Kamera") }
        },
        dismissButton = {
            TextButton(onClick = onGalleryClick) { Text("Galeri") }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hapus Gambar") },
        text = { Text("Anda yakin ingin menghapus gambar ini?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) { Text("Hapus") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenImageViewer(
    images: List<Any>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    onDelete: (Any) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { images.size }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.9f)) {
            Box(contentAlignment = Alignment.Center) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
                        scale = (scale * zoomChange).coerceIn(1f, 5f)
                        val newOffsetX = offset.x + panChange.x * scale
                        val newOffsetY = offset.y + panChange.y * scale
                        // Simple bounds to prevent panning too far
                        val maxX = (scale - 1) * 500
                        val maxY = (scale - 1) * 800
                        offset = Offset(newOffsetX.coerceIn(-maxX, maxX), newOffsetY.coerceIn(-maxY, maxY))
                    }

                    AsyncImage(
                        model = images[page],
                        contentDescription = "Gambar Penuh ${page + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            .transformable(state = transformableState)
                            .clickable {
                                // Reset zoom and pan on simple click
                                scale = 1f
                                offset = Offset.Zero
                            }
                    )
                }

                // Top bar with Delete and Close buttons
                Row(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                    IconButton(onClick = { onDelete(images[pagerState.currentPage]) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.White)
                    }
                }


                // Indicator
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)

                ) {
                    repeat(images.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) Color.White else Color.Gray
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(10.dp)
                        )
                    }
                }
            }
        }
    }
}
