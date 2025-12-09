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
import com.rahma.findingpatrol.data.model.finding.Finding
import com.rahma.findingpatrol.data.model.metadata.Category
import com.rahma.findingpatrol.ui.components.FormImagePicker
import com.rahma.findingpatrol.ui.components.FormRow
import com.rahma.findingpatrol.ui.components.MetadataDropdown
import com.rahma.findingpatrol.viewmodel.finding.CreateFindingViewModel
import com.rahma.findingpatrol.viewmodel.finding.FindingDetailState
import com.rahma.findingpatrol.viewmodel.finding.FindingDetailViewModel
import com.rahma.findingpatrol.viewmodel.metadata.MetadataViewModel
import com.rahma.findingpatrol.viewmodel.metadata.MetadataState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEditFindingScreen(
    viewModel: CreateFindingViewModel,
    metadataViewModel: MetadataViewModel,
    findingDetailViewModel: FindingDetailViewModel,
    onNavigateBack: () -> Unit,
    findingId: Int = 0,
    showCategory: Boolean = false
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // State form
    var findingText by remember { mutableStateOf("") }
    var areaText by remember { mutableStateOf("") }
    var countermeasureText by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    
    // Metadata State
    var selectedPatrol by remember { mutableStateOf<com.rahma.findingpatrol.data.model.metadata.Patrol?>(null) }
    var selectedPic by remember { mutableStateOf<com.rahma.findingpatrol.data.model.metadata.Pic?>(null) }
    var selectedPriority by remember { mutableStateOf<com.rahma.findingpatrol.data.model.metadata.Priority?>(null) }
    var selectedRiskLevel by remember { mutableStateOf<com.rahma.findingpatrol.data.model.metadata.RiskLevel?>(null) }
    var selectedFindingType by remember { mutableStateOf<com.rahma.findingpatrol.data.model.metadata.FindingType?>(null) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    // Dropdown Expanded States
    var patrolExpanded by remember { mutableStateOf(false) }
    var picExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var riskExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // Load Metadata
    val metadataState by metadataViewModel.metadataState.collectAsState()
    LaunchedEffect(Unit) {
        metadataViewModel.loadMetadata()
        viewModel.resetState()
    }

    // Fetch Finding Detail if findingId > 0
    LaunchedEffect(findingId) {
        if (findingId > 0) {
            findingDetailViewModel.getFindingDetail(findingId)
        }
    }
    
    val detailState by findingDetailViewModel.state.collectAsState()
    val isEditMode = findingId > 0

    // Pre-fill Data
    LaunchedEffect(metadataState, detailState, isEditMode) {
        if (isEditMode && detailState is FindingDetailState.Success && metadataState is MetadataState.Success) {
            val data = (metadataState as MetadataState.Success).data
            val detail = (detailState as FindingDetailState.Success).data
            
            findingText = detail.finding
            areaText = detail.area
            countermeasureText = detail.countermeasureSuggestion ?: ""
            
            // Match Metadata
            selectedPatrol = data.patrol.find { it.patrolName == detail.patrolName }
            selectedPriority = data.priority.find { it.priorityName == detail.priorityName }
            selectedPic = data.pics.find { it.sectionName == detail.picName }
            selectedRiskLevel = data.riskLevel.find { it.riskLevel == detail.riskLevelName }
            selectedFindingType = data.findingType.find { it.typeName == detail.findingTypeName }
            selectedCategory = data.category.find { it.categoryName == detail.categoryName }
        }
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedImageUris = uris
    }

    // Submit Action
    val loading by viewModel.loading.collectAsState()
    val success by viewModel.success.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(success) {
        if (success) {
            val message = if (isEditMode) "Finding updated successfully!" else "Finding created successfully!"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }
    
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = { },
        bottomBar = {
             Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
            }
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
                     Icon(
                         imageVector = Icons.Default.Close,
                         contentDescription = "Close"
                     )
                 }
            }
            
            Text(
                text = if (isEditMode) "Edit Finding" else "Form Patrol",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Buttons Batal / Simpan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0), contentColor = Color.Black),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.width(120.dp)
                ) {
                    Text("Batal")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                         if (findingText.isBlank() || areaText.isBlank() || selectedPatrol == null || (selectedImageUris.isEmpty() && !isEditMode)) {
                             Toast.makeText(context, "Please fill required fields (*)", Toast.LENGTH_SHORT).show()
                         } else {
                             if (isEditMode) {
                                 viewModel.updateFinding(
                                     context = context,
                                     id = findingId,
                                     finding = findingText,
                                     area = areaText,
                                     patrolId = selectedPatrol!!.patrolId,
                                     picId = selectedPic?.sectionId,
                                     priorityId = selectedPriority?.priorityId,
                                     riskLevelId = selectedRiskLevel?.riskLevelId,
                                     findingTypeId = selectedFindingType?.findingTypeId,
                                     categoryId = selectedCategory?.categoryId,
                                     countermeasureSuggestion = countermeasureText,
                                     imageUris = selectedImageUris
                                 )
                             } else {
                                 viewModel.createFinding(
                                     context = context,
                                     finding = findingText,
                                     area = areaText,
                                     patrolId = selectedPatrol!!.patrolId,
                                     picId = selectedPic?.sectionId,
                                     priorityId = selectedPriority?.priorityId,
                                     riskLevelId = selectedRiskLevel?.riskLevelId,
                                     findingTypeId = selectedFindingType?.findingTypeId,
                                     categoryId = selectedCategory?.categoryId,
                                     countermeasureSuggestion = countermeasureText,
                                     imageUris = selectedImageUris
                                 )
                             }
                         }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD7E8CD), contentColor = Color.Black),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.width(120.dp),
                    enabled = !loading
                ) {
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text(if (isEditMode) "Update" else "Simpan")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "* Indicates required questions",
                color = Color.Red,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Form Fields
            
            // 1. Finding *
            FormRow(
                label = "Finding *",
                content = {
                    OutlinedTextField(
                        value = findingText,
                        onValueChange = { findingText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Finding") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = Color.Black
                        )
                    )
                }
            )
            
            // 2. Finding Pictures *
            var imagePlaceholder = "IMG.png"
            if (isEditMode && detailState is FindingDetailState.Success) {
                val detail = (detailState as FindingDetailState.Success).data
                if (detail.images.isNotEmpty()) {
                    imagePlaceholder = "Keep existing ${detail.images.size} images"
                }
            }
            
            FormImagePicker(
                label = "Finding Pictures *",
                selectedImageUris = selectedImageUris,
                onPickImage = { imagePickerLauncher.launch("image/*") },
                placeholderText = imagePlaceholder
            )

            // 3. Patrol *
            FormRow(
                label = "Patrol *",
                content = {
                    MetadataDropdown(
                        label = "Select Patrol",
                        options = (metadataState as? MetadataState.Success)?.data?.patrol ?: emptyList(),
                        selectedOption = selectedPatrol,
                        onOptionSelected = { selectedPatrol = it },
                        itemLabel = { it.patrolName },
                        expanded = patrolExpanded,
                        onExpandedChange = { patrolExpanded = it }
                    )
                }
            )

            // 4. Area *
            FormRow(
                label = "Area *",
                content = {
                     OutlinedTextField(
                        value = areaText,
                        onValueChange = { areaText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Area") },
                        singleLine = true,
                         colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = Color.Black
                        )
                    )
                }
            )
            
            // 5. PIC
            FormRow(
                label = "PIC",
                content = {
                    MetadataDropdown(
                        label = "Select PIC",
                        options = (metadataState as? MetadataState.Success)?.data?.pics ?: emptyList(),
                        selectedOption = selectedPic,
                        onOptionSelected = { selectedPic = it },
                        itemLabel = { it.sectionName },
                        expanded = picExpanded,
                        onExpandedChange = { picExpanded = it }
                    )
                }
            )
            
            // 6. Priority
            FormRow(
                label = "Priority",
                content = {
                    MetadataDropdown(
                        label = "Select Priority",
                        options = (metadataState as? MetadataState.Success)?.data?.priority ?: emptyList(),
                        selectedOption = selectedPriority,
                        onOptionSelected = { selectedPriority = it },
                        itemLabel = { it.priorityName },
                        expanded = priorityExpanded,
                        onExpandedChange = { priorityExpanded = it }
                    )
                }
            )
            
            // 7. Risk Level
             FormRow(
                label = "Risk Level",
                content = {
                    MetadataDropdown(
                        label = "Select Risk Level",
                        options = (metadataState as? MetadataState.Success)?.data?.riskLevel ?: emptyList(),
                        selectedOption = selectedRiskLevel,
                        onOptionSelected = { selectedRiskLevel = it },
                        itemLabel = { it.riskLevel },
                        expanded = riskExpanded,
                        onExpandedChange = { riskExpanded = it }
                    )
                }
            )
            
            // 8. Finding Type
             FormRow(
                label = "Finding Type",
                content = {
                    MetadataDropdown(
                        label = "Select Type",
                        options = (metadataState as? MetadataState.Success)?.data?.findingType ?: emptyList(),
                        selectedOption = selectedFindingType,
                        onOptionSelected = { selectedFindingType = it },
                        itemLabel = { it.typeName },
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = it }
                    )
                }
            )
            
            // 9. Category (Conditional)
            if (showCategory) {
                FormRow(
                    label = "Category",
                    content = {
                        MetadataDropdown(
                            label = "Select Category",
                            options = (metadataState as? MetadataState.Success)?.data?.category ?: emptyList(),
                            selectedOption = selectedCategory,
                            onOptionSelected = { selectedCategory = it },
                            itemLabel = { it.categoryName },
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = it }
                        )
                    }
                )
            }
            
            // 10. Suggestions
            FormRow(
                label = "Countermeasure Suggestion",
                content = {
                     OutlinedTextField(
                        value = countermeasureText,
                        onValueChange = { countermeasureText = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        placeholder = { Text("Suggestions") },
                        maxLines = 5,
                         colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = Color.Black
                        )
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // Bottom Home Icon
             Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                     painter = painterResource(id = R.drawable.ic_home), 
                     contentDescription = "Home",
                     modifier = Modifier.size(40.dp).clickable { onNavigateBack() }
                )
            }
        }
    }
}
