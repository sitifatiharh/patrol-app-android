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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rahma.findingpatrol.ui.components.DeleteConfirmationDialog
import com.rahma.findingpatrol.ui.components.FormRow
import com.rahma.findingpatrol.ui.components.FullScreenImageViewer
import com.rahma.findingpatrol.ui.components.ImagePreviewGrid
import com.rahma.findingpatrol.ui.components.ImageSourceSelectionDialog
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
    var screenTitle by rememberSaveable { mutableStateOf("Form Perbaikan Temuan") }
    var buttonText by rememberSaveable { mutableStateOf("Simpan Perbaikan") }
    var isReadyForRecheck by rememberSaveable { mutableStateOf(false) }

    // UI State
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Any?>(null) }
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
            isReadyForRecheck = detail.statusName.uppercase() == "READY FOR RECHECK"
            screenTitle = if (isReadyForRecheck) "Form Edit Perbaikan" else "Form Perbaikan Temuan"
            buttonText = if (isReadyForRecheck) "Edit Perbaikan" else "Simpan Perbaikan"
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

    // Submit Logic
    val loading by viewModel.loading.collectAsState()
    val success by viewModel.success.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(success) {
        if (success) {
            val message = if (isReadyForRecheck) "Perbaikan berhasil di-edit!" else "Temuan berhasil diperbaiki!"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }
    LaunchedEffect(error) { error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(screenTitle, fontWeight = FontWeight.Bold) },
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

            Spacer(modifier = Modifier.height(16.dp))

            FormRow(label = "Countermeasure", isMandatory = true) {
                OutlinedTextField(
                    value = countermeasureText,
                    onValueChange = { countermeasureText = it },
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 120.dp),
                    placeholder = { Text("Jelaskan tindakan perbaikan yang dilakukan...") },
                )
            }

            if (isOverdue) {
                FormRow(label = "Delay Reason", isMandatory = true) {
                    OutlinedTextField(
                        value = delayReasonText,
                        onValueChange = { delayReasonText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Jelaskan alasan keterlambatan...") },
                    )
                }
            }

            FormRow(label = "Improved Pictures", isMandatory = true) {
                ImagePreviewGrid(
                    existingImages = existingImages,
                    selectedImageUris = selectedImageUris,
                    onAddImageClick = { showImageSourceDialog = true },
                    onImageClick = { images, index -> showImageViewer = images to index },
                    onDeleteClick = { image -> showDeleteDialog = image }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Action Buttons ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.outlinedButtonColors(),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) { Text("Batal", fontSize = 16.sp) }

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
                            viewModel.updateFindingByPIC(
                                context = context,
                                id = findingId,
                                countermeasure = countermeasureText,
                                delayReason = if (isOverdue) delayReasonText else "",
                                imageUris = selectedImageUris
                            )
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    enabled = !loading
                ) {
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White) else Text(buttonText, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}
