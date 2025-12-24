package com.rahma.findingpatrol.ui.finding

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rahma.findingpatrol.util.fixImageUrl
import com.rahma.findingpatrol.viewmodel.finding.CreateFindingViewModel
import com.rahma.findingpatrol.viewmodel.finding.FindingDetailState
import com.rahma.findingpatrol.viewmodel.finding.FindingDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PicScreen(
    navController: NavController,
    viewModel: CreateFindingViewModel,
    findingDetailViewModel: FindingDetailViewModel,
    onNavigateBack: () -> Unit,
    findingId: Int,
    isOverdue: Boolean = false
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // State
    var countermeasureText by rememberSaveable { mutableStateOf("") }
    var delayReasonText by rememberSaveable { mutableStateOf("") }
    var selectedImageUris by rememberSaveable { mutableStateOf<List<Uri>>(emptyList()) }
    var existingImages by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    // UI State
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Any?>(null) } // Can be Uri or String
    var showImageViewer by remember { mutableStateOf<Pair<List<Any>, Int>?>(null) }

    // Data Loading
    val detailState by findingDetailViewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        if (findingId > 0) {
            findingDetailViewModel.getFindingDetail(findingId)
        }
        viewModel.resetState()
    }

    LaunchedEffect(detailState) {
        if (detailState is FindingDetailState.Success) {
            val detail = (detailState as FindingDetailState.Success).data
            if (countermeasureText.isEmpty()) {
                countermeasureText = detail.countermeasure ?: ""
            }
            if (existingImages.isEmpty()) {
                existingImages = detail.images
                    .filter { it.imageType == "improved" }
                    .map { fixImageUrl(it.imageUrl) }
            }
        }
    }

    // Image Launchers
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
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

    // Dialogs
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
                val updatedImages = images.filter { it != image }
                if (updatedImages.isEmpty()) {
                    showImageViewer = null
                } else {
                    showImageViewer = updatedImages to 0
                }
            }
        )
    }

    // Submit Logic
    val loading by viewModel.loading.collectAsState()
    val success by viewModel.success.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(success) {
        if (success) {
            Toast.makeText(context, "Temuan berhasil diperbaiki!", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }
    LaunchedEffect(error) { error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Form Perbaikan Temuan") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Kembali") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            AnnotatedLabel(label = "Tindakan Perbaikan *")
            OutlinedTextField(
                value = countermeasureText,
                onValueChange = { countermeasureText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Jelaskan tindakan perbaikan yang dilakukan...") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(16.dp))


            if (isOverdue) {
                AnnotatedLabel(label = "Alasan Keterlambatan *")
                OutlinedTextField(
                    value = delayReasonText,
                    onValueChange = { delayReasonText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Jelaskan alasan keterlambatan...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Gray,
                        focusedBorderColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            FindingFormImagePicker(
                label = "Foto Bukti Perbaikan *",
                imageUris = selectedImageUris,
                existingImageUrls = existingImages,
                onAddImageClick = { showImageSourceDialog = true },
                onRemoveUri = { uri -> showDeleteDialog = uri },
                onRemoveUrl = { url -> showDeleteDialog = url },
                onImageClick = { allImages, index -> showImageViewer = allImages to index }
            )

            Spacer(Modifier.height(32.dp))

            // --- Action Buttons ---
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) { Text("Batal", fontSize = 19.sp) }

                Button(
                    onClick = {
                        val isDelayReasonValid = !isOverdue || delayReasonText.isNotBlank()
                        val hasImages = selectedImageUris.isNotEmpty() || existingImages.isNotEmpty()

                        if (countermeasureText.isBlank() || !isDelayReasonValid || !hasImages) {
                            val msg = when {
                                countermeasureText.isBlank() -> "Harap isi Tindakan Perbaikan (*)"
                                !isDelayReasonValid -> "Harap isi Alasan Keterlambatan (*)"
                                else -> "Harap tambah setidaknya satu foto bukti (*)"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateFinding(
                                context = context,
                                id = findingId,
                                countermeasure = countermeasureText,
                                delayReason = if (isOverdue) delayReasonText else null,
                                newImprovedImageUris = selectedImageUris
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD7E8CD), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = !loading
                ) {
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Simpan", fontSize = 19.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AnnotatedLabel(label: String) {
    val annotatedString = buildAnnotatedString {
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
    Text(
        text = annotatedString,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
