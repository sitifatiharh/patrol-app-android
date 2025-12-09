package com.rahma.findingpatrol.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FormRow(
    label: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top 
    ) {
        // Label Box (Gray Background)
        Box(
            modifier = Modifier
                .width(130.dp) // Fixed width for labels
                .background(Color(0xFFDDE3EA), RoundedCornerShape(8.dp))
                .padding(vertical = 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val labelText = label.replace("*", "")
            val isRequired = label.contains("*")
            
            Row {
                Text(
                    text = labelText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                if (isRequired) {
                    Text(text = "*", color = Color.Red, fontSize = 13.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Input Content
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

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
    enabled: Boolean = true // Added enabled param
) {
    ExposedDropdownMenuBox(
        expanded = if (enabled) expanded else false,
        onExpandedChange = { if (enabled) onExpandedChange(it) }
    ) {
        OutlinedTextField(
            value = if (selectedOption != null) itemLabel(selectedOption) else "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled, // Pass enabled
            placeholder = { Text(label) },
            trailingIcon = { if (enabled) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Gray,
                focusedBorderColor = Color.Black,
                disabledBorderColor = Color.LightGray,
                disabledTextColor = Color.Gray
            ),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        if (enabled) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(itemLabel(option)) },
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
fun FormImagePicker(
    label: String,
    selectedImageUris: List<Uri>,
    onPickImage: () -> Unit,
    placeholderText: String = "IMG.png",
    enabled: Boolean = true // Added enabled param
) {
    FormRow(
        label = label,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, if (enabled) Color.Gray else Color.LightGray, RoundedCornerShape(4.dp))
                    .then(if (enabled) Modifier.clickable { onPickImage() } else Modifier)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayText = if (selectedImageUris.isNotEmpty()) {
                        "${selectedImageUris.size} images selected"
                    } else {
                        placeholderText
                    }
                    
                    Text(
                        text = displayText,
                        color = if (selectedImageUris.isEmpty() && placeholderText == "IMG.png") Color.Gray else Color.Black
                    )
                    if (enabled) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Pick Image", tint = Color.Gray)
                    }
                }
            }
        }
    )
}
