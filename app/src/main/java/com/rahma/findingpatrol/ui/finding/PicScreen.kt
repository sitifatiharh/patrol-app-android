package com.rahma.findingpatrol.ui.finding

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahma.findingpatrol.R
import com.rahma.findingpatrol.ui.components.FormImagePicker
import com.rahma.findingpatrol.ui.components.FormRow
import com.rahma.findingpatrol.viewmodel.finding.CreateFindingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PicScreen(
    viewModel: CreateFindingViewModel,
    onNavigateBack: () -> Unit,
    findingId: Int,
    isOverdue: Boolean = false
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // State
    var countermeasureText by remember { mutableStateOf("") }
    var delayReasonText by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedImageUris = uris
    }

    // Submit Logic
    val loading by viewModel.loading.collectAsState()
    val success by viewModel.success.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    LaunchedEffect(success) {
        if (success) {
            Toast.makeText(context, "Finding resolved successfully!", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    LaunchedEffect(error) {
        error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    Scaffold(
        topBar = { },
        bottomBar = {
             Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) { }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                 Spacer(modifier = Modifier.weight(1f))
                 IconButton(onClick = onNavigateBack) {
                     Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                 }
            }
            
            Text(
                text = "Resolve Finding (PIC)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0), contentColor = Color.Black),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.width(120.dp)
                ) { Text("Batal") }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Button(
                    onClick = {
                        val isDelayReasonValid = if (isOverdue) delayReasonText.isNotBlank() else true
                        
                        if (countermeasureText.isBlank() || !isDelayReasonValid || selectedImageUris.isEmpty()) {
                            val msg = if (isOverdue && delayReasonText.isBlank()) "Please fill Delay Reason (*)" else "Please fill Countermeasure & Images (*)"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateFinding(
                                context = context,
                                id = findingId,
                                finding = "",
                                area = "",
                                patrolId = 0,
                                picId = null,
                                priorityId = null,
                                riskLevelId = null,
                                findingTypeId = null,
                                categoryId = null,
                                countermeasureSuggestion = null,
                                countermeasure = countermeasureText,
                                delayReason = if (isOverdue) delayReasonText else null,
                                imageUris = selectedImageUris
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD7E8CD), contentColor = Color.Black),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.width(120.dp),
                    enabled = !loading
                ) {
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Simpan")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Form Fields
            
            // 1. Countermeasure *
            FormRow(
                label = "Countermeasure *",
                content = {
                     OutlinedTextField(
                        value = countermeasureText,
                        onValueChange = { countermeasureText = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        placeholder = { Text("Action taken...") },
                        maxLines = 5,
                         colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = Color.Black
                        )
                    )
                }
            )

            // 2. Delay Reason (Conditional) *
            if (isOverdue) {
                FormRow(
                    label = "Delay Reason *",
                    content = {
                         OutlinedTextField(
                            value = delayReasonText,
                            onValueChange = { delayReasonText = it },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            placeholder = { Text("Why is it overdue?") },
                            maxLines = 3,
                             colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Gray,
                                focusedBorderColor = Color.Black
                            )
                        )
                    }
                )
            }
            
            // 3. Evidence Photos *
            FormImagePicker(
                label = "Evidence Photos *",
                selectedImageUris = selectedImageUris,
                onPickImage = { imagePickerLauncher.launch("image/*") }
            )
        }
    }
}
