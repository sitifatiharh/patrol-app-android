package com.rahma.findingpatrol.ui.metadata

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.rahma.findingpatrol.util.fixImageUrl
import kotlinx.coroutines.launch

// Unified FormRow with red asterisk handling
@Composable
fun FormRow(label: String, content: @Composable () -> Unit) {
    val annotatedLabel = buildAnnotatedString {
        if (label.trim().endsWith("*")) {
            append(label.substringBeforeLast("*"))
            withStyle(style = SpanStyle(color = Color.Red)) {
                append("*")
            }
        } else {
            append(label)
        }
    }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = annotatedLabel,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

// Unified MetadataDropdown
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> MetadataDropdown(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    ExposedDropdownMenuBox(
        expanded = if (enabled) expanded else false,
        onExpandedChange = { if (enabled) onExpandedChange(it) }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedOption?.let { itemLabel(it) } ?: "",
            onValueChange = { },
            enabled = enabled,
            label = null,
            placeholder = { Text(label, fontSize = 17.sp) },
            trailingIcon = { if (enabled) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Gray,
                focusedBorderColor = Color.Black,
                disabledBorderColor = Color.LightGray,
                disabledTextColor = Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled),
            singleLine = true,
            textStyle = TextStyle(fontSize = 17.sp)
        )
        if (enabled) {
             ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = itemLabel(option), fontSize = 17.sp) },
                        onClick = {
                            onOptionSelected(option)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FormSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}


// Unified FormImagePicker with advanced features
@Composable
fun FormImagePicker(
    label: String,
    selectedImageUris: List<Uri>,
    existingImages: List<String>,
    onPickImage: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onRemoveExisting: (String) -> Unit,
    placeholderText: String,
    enabled: Boolean = true
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedImageUris.size) {
        if (selectedImageUris.isNotEmpty()) {
            coroutineScope.launch {
                scrollState.animateScrollTo(0)
            }
        }
    }

    FormRow(label = label) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUris.isEmpty() && existingImages.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (enabled) Modifier.clickable { onPickImage() } else Modifier),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Add Photo",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        Text(text = placeholderText, color = Color.Gray, fontSize = 17.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (enabled && existingImages.size + selectedImageUris.size < 10) {
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                                    .clickable { onPickImage() },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add More",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text("Tambah", fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                        }

                        selectedImageUris.forEach { uri ->
                            Box(modifier = Modifier.size(140.dp)) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        ImageRequest.Builder(LocalContext.current).data(uri).size(Size.ORIGINAL).build()
                                    ),
                                    contentDescription = "New Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.LightGray, RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                if (enabled) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(24.dp)
                                            .background(Color.Red, CircleShape)
                                            .clickable { onRemoveImage(uri) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        existingImages.forEach { url ->
                            Box(modifier = Modifier.size(140.dp)) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(fixImageUrl(url))
                                        .crossfade(true)
                                        .size(Size.ORIGINAL)
                                        .build(),
                                    contentDescription = "Existing Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.LightGray, RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                if (enabled) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(24.dp)
                                            .background(Color.Red, CircleShape)
                                            .clickable { onRemoveExisting(url) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
