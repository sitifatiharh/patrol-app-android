package com.rahma.findingpatrol.ui.components

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

@Composable
fun FormRow(
    label: String,
    isMandatory: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = buildAnnotatedString {
                append(label)
                if (isMandatory) {
                    withStyle(style = MaterialTheme.typography.bodyMedium.toSpanStyle().copy(color = Color.Red)) {
                        append(" *")
                    }
                }
            },
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetadataDropdown(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    readOnly: Boolean = false
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (!readOnly) onExpandedChange(it) }) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {}, // read-only
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = {
                if (!readOnly) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle menu"
                    )
                }
            }
        )
        if (!readOnly) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.exposedDropdownSize(true)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                        }
                    )
                }
            }
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
        text = {
            Column {
                ListItem(
                    headlineContent = { Text("Buka Kamera") },
                    leadingContent = { Icon(Icons.Default.PhotoCamera, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = onCameraClick)
                )
                ListItem(
                    headlineContent = { Text("Pilih dari Galeri") },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = onGalleryClick)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hapus Gambar?") },
        text = { Text("Anda yakin ingin menghapus gambar ini?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
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
    onDelete: ((Any) -> Unit)? = null, // Make onDelete optional
    onIndexChanged: (Int) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { images.size })

    LaunchedEffect(pagerState.currentPage) {
        if (images.isNotEmpty()) {
            onIndexChanged(pagerState.currentPage)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.9f)) {
            Box(contentAlignment = Alignment.Center) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = true // Allow swipe gestures
                ) { page ->
                    var scale by remember { mutableStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }
                    val animatedScale by animateFloatAsState(targetValue = scale, label = "scale")

                    LaunchedEffect(page) {
                        scale = 1f
                        offset = Offset.Zero
                    }

                    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
                        scale = (scale * zoomChange).coerceIn(1f, 4f)
                        val maxPan = (scale - 1f) * 500f
                        offset = Offset(
                            x = (offset.x + panChange.x).coerceIn(-maxPan, maxPan),
                            y = (offset.y + panChange.y).coerceIn(-maxPan, maxPan)
                        )
                    }

                    AsyncImage(
                        model = images.getOrNull(page),
                        contentDescription = "Gambar Penuh ${page + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        scale = if (scale > 1f) 1f else 2.5f
                                        offset = Offset.Zero
                                    }
                                )
                            }
                            .graphicsLayer(
                                scaleX = animatedScale,
                                scaleY = animatedScale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            .transformable(state = transformableState)
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                    if (pagerState.pageCount > 0) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${pagerState.pageCount}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (onDelete != null) {
                            IconButton(onClick = { onDelete(images[pagerState.currentPage]) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.White)
                            }
                        } else {
                            Spacer(modifier = Modifier.size(48.dp)) // Placeholder for balance
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}


@Composable
fun ImagePreviewGrid(
    existingImages: List<String>,
    selectedImageUris: List<Uri>,
    onAddImageClick: () -> Unit,
    onImageClick: (List<Any>, Int) -> Unit,
    onDeleteClick: (Any) -> Unit,
    readOnly: Boolean = false
) {
    val allImages: List<Any> = selectedImageUris + existingImages
    val totalImages = allImages.size

    Column {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            if (!readOnly && totalImages < 10) {
                item {
                    AddImageButton(onClick = onAddImageClick)
                }
            }

            itemsIndexed(allImages) { index, image ->
                Box(
                    modifier = Modifier.size(100.dp)
                ) {
                    AsyncImage(
                        model = image,
                        contentDescription = "Gambar ${index + 1}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onImageClick(allImages, index) },
                        contentScale = ContentScale.Crop
                    )
                    if (!readOnly) {
                        IconButton(
                            onClick = { onDeleteClick(image) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Hapus gambar",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
        Text(
            text = "$totalImages/10 gambar",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun AddImageButton(onClick: () -> Unit) {
    val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
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
            drawRoundRect(color = Color.Gray, style = stroke, cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Default.PhotoCamera, contentDescription = "Tambah Gambar", tint = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text("Tambah Gambar", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun CancelButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = Color.White
        )
    ) {
        Text("Batal", fontSize = 16.sp)
    }
}