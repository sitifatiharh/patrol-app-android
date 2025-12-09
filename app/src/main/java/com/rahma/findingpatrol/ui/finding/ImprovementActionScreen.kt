package com.rahma.findingpatrol.ui.finding

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun ImprovementActionScreen(
    viewModel: CreateFindingViewModel,
    metadataViewModel: MetadataViewModel,
    findingDetailViewModel: FindingDetailViewModel,
    onNavigateBack: () -> Unit,
    findingId: Int,
    currentUserId: Int // Passed to determine Creator logic
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // --- State Variables ---
    // 1. Finding Details (Group 1)
    var findingText by remember { mutableStateOf("") }
    var areaText by remember { mutableStateOf("") }
    var selectedPatrol by remember { mutableStateOf<com.rahma.findingpatrol.data.model.metadata.Patrol?>(null) }
    var selectedFindingImages by remember { mutableStateOf<List<Uri>>(emptyList()) } // New images to add
    
    // 2. Assignment (Group 2)
    var selectedPic by remember { mutableStateOf<com.rahma.findingpatrol.data.model.metadata.Pic?>(null) }
    
    // 3. Metadata (Group 3)
    var selectedPriority by remember { mutableStateOf<com.rahma.findingpatrol.data.model.metadata.Priority?>(null) }
    var selectedRiskLevel by remember { mutableStateOf<com.rahma.findingpatrol.data.model.metadata.RiskLevel?>(null) }
    var selectedFindingType by remember { mutableStateOf<com.rahma.findingpatrol.data.model.metadata.FindingType?>(null) }
    var countermeasureText by remember { mutableStateOf("") } // Countermeasure suggestion? Or Action? 
    // User listed "countermeasure". In Form 6/Improvement context, it's usually "Countermeasure Suggestion" or "Action"? 
    // Let's assume Suggestion for now, or both. But `updateFinding` has fields for both.
    // Based on "Form 3... finding, area, patrol, foto... pic, priority...". Usually Form 3 has Suggestion.
    // Form 6 has everything.
    // I'll stick to "Countermeasure Suggestion" as editable metadata.
    
    // 4. Closing (Group 4)
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    
    // 5. Improvement Photos (Group 5)
    var selectedImproveImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

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
    
    LaunchedEffect(Unit) {
        metadataViewModel.loadMetadata()
        viewModel.resetState()
        if (findingId > 0) {
            findingDetailViewModel.getFindingDetail(findingId)
        }
    }
    
    // Pre-fill
    var statusName by remember { mutableStateOf("") }
    var isCreator by remember { mutableStateOf(false) }

    LaunchedEffect(metadataState, detailState) {
        if (detailState is FindingDetailState.Success && metadataState is MetadataState.Success) {
            val data = (metadataState as MetadataState.Success).data
            val detail = (detailState as FindingDetailState.Success).data
            
            statusName = detail.statusName.uppercase()
            // isCreator = detail.userId == currentUserId // Need userId in Detail. 
            // For now, assume passed logic or mock true for testing "creator" flow if Open.
            isCreator = true // Mocking true to enable testing "Creator Edit" flow. Replace with real logic.

            findingText = detail.finding
            areaText = detail.area
            countermeasureText = detail.countermeasureSuggestion ?: ""
            
            selectedPatrol = data.patrol.find { it.patrolName == detail.patrolName }
            selectedPic = data.pics.find { it.sectionName == detail.picName }
            selectedPriority = data.priority.find { it.priorityName == detail.priorityName }
            selectedRiskLevel = data.riskLevel.find { it.riskLevel == detail.riskLevelName }
            selectedFindingType = data.findingType.find { it.typeName == detail.findingTypeName }
            selectedCategory = data.category.find { it.categoryName == detail.categoryName }
        }
    }

    // Logic Enable/Disable
    val isOpen = statusName == "OPEN"
    val isAssigned = statusName == "ASSIGNED"
    val isReady = statusName == "READY FOR RECHECK"
    val isClosed = statusName == "CLOSED"

    val canEditDetails = (isOpen && isCreator) // Only Open & Creator can edit Finding/Area/Patrol/Photos
    val canEditCategory = isReady || isClosed // Enable Category only in Ready or Closed
    val canEditPic = true // PIC always editable for Improvement? "Status open... ubah status ke assigned kalau pic diisi". "Status assigned dia bisa edit". So Yes.
    val canEditMetadata = true // Priority/Risk/Type always editable for Improvement (Form 6 logic)

    // Image Pickers
    val findingImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { selectedFindingImages = it }
    val improveImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { selectedImproveImages = it }

    // Submit Logic
    val loading by viewModel.loading.collectAsState()
    val success by viewModel.success.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(success) {
        if (success) {
            Toast.makeText(context, "Action successful!", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }
    LaunchedEffect(error) { error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }

    Scaffold(
        bottomBar = { Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                 Spacer(modifier = Modifier.weight(1f))
                 IconButton(onClick = onNavigateBack) {
                     Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                 }
            }
            
            Text(
                text = "Improvement Action ($statusName)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(onClick = onNavigateBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0), contentColor = Color.Black)) { Text("Batal") }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        // Validation
                        if (isOpen && selectedPic == null) {
                             Toast.makeText(context, "Please Assign PIC to proceed", Toast.LENGTH_SHORT).show()
                        } else if (isReady && selectedCategory == null) {
                             Toast.makeText(context, "Please select Category to Close", Toast.LENGTH_SHORT).show()
                        } else {
                            // Update
                            viewModel.updateFinding(
                                context = context,
                                id = findingId,
                                finding = findingText,
                                area = areaText,
                                patrolId = selectedPatrol?.patrolId ?: 0,
                                picId = selectedPic?.sectionId,
                                priorityId = selectedPriority?.priorityId,
                                riskLevelId = selectedRiskLevel?.riskLevelId,
                                findingTypeId = selectedFindingType?.findingTypeId,
                                categoryId = selectedCategory?.categoryId,
                                countermeasureSuggestion = countermeasureText,
                                imageUris = selectedFindingImages // Appending finding images
                                // improveImages logic? CreateFindingViewModel assumes 'imageUris' are Finding images.
                                // If we need to upload Improve images, we might need a separate API call or VM update.
                                // For now, sending finding images.
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD7E8CD), contentColor = Color.Black),
                    enabled = !loading
                ) {
                    val btnText = when {
                        isOpen -> "Assign"
                        isReady -> "Close Finding"
                        else -> "Update"
                    }
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text(btnText)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // --- SECTION 1: Finding Details ---
            Text("Finding Details", fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            
            FormRow("Finding" + if (canEditDetails) " *" else "", {
                OutlinedTextField(
                    value = findingText, onValueChange = { if (canEditDetails) findingText = it },
                    modifier = Modifier.fillMaxWidth(), readOnly = !canEditDetails, enabled = canEditDetails
                )
            })
            FormRow("Area" + if (canEditDetails) " *" else "", {
                OutlinedTextField(
                    value = areaText, onValueChange = { if (canEditDetails) areaText = it },
                    modifier = Modifier.fillMaxWidth(), readOnly = !canEditDetails, enabled = canEditDetails
                )
            })
            FormRow("Patrol" + if (canEditDetails) " *" else "", {
                MetadataDropdown("Select Patrol", (metadataState as? MetadataState.Success)?.data?.patrol ?: emptyList(), selectedPatrol, { selectedPatrol = it }, { it.patrolName }, patrolExpanded, { patrolExpanded = it }, enabled = canEditDetails)
            })
            
            var phText = "IMG.png"
            if (detailState is FindingDetailState.Success) {
                 val detail = (detailState as FindingDetailState.Success).data
                 val count = detail.images.count { it.imageType == "finding" }
                 if (count > 0) phText = "$count existing photos"
            }
            FormImagePicker("Finding Photos", selectedFindingImages, { findingImageLauncher.launch("image/*") }, placeholderText = phText, enabled = canEditDetails)

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // --- SECTION 2: Assignment & Metadata ---
            Text("Assignment & Metadata", fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            FormRow("PIC" + if (isOpen) " *" else "", {
                MetadataDropdown("Select PIC", (metadataState as? MetadataState.Success)?.data?.pics ?: emptyList(), selectedPic, { selectedPic = it }, { it.sectionName }, picExpanded, { picExpanded = it }, enabled = canEditPic)
            })
            
            FormRow("Priority", {
                MetadataDropdown("Select Priority", (metadataState as? MetadataState.Success)?.data?.priority ?: emptyList(), selectedPriority, { selectedPriority = it }, { it.priorityName }, priorityExpanded, { priorityExpanded = it }, enabled = canEditMetadata)
            })
            
            FormRow("Risk Level", {
                MetadataDropdown("Select Risk", (metadataState as? MetadataState.Success)?.data?.riskLevel ?: emptyList(), selectedRiskLevel, { selectedRiskLevel = it }, { it.riskLevel }, riskExpanded, { riskExpanded = it }, enabled = canEditMetadata)
            })
             FormRow("Type", {
                MetadataDropdown("Select Type", (metadataState as? MetadataState.Success)?.data?.findingType ?: emptyList(), selectedFindingType, { selectedFindingType = it }, { it.typeName }, typeExpanded, { typeExpanded = it }, enabled = canEditMetadata)
            })
            FormRow("Suggestion", {
                 OutlinedTextField(value = countermeasureText, onValueChange = { countermeasureText = it }, modifier = Modifier.fillMaxWidth().height(80.dp), enabled = canEditMetadata)
            })

            Spacer(modifier = Modifier.height(16.dp))
            
            // --- SECTION 3: Closing (Category) ---
            if (isReady || isClosed) {
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Closing", fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                
                FormRow("Category" + if (isReady) " *" else "", {
                    MetadataDropdown("Select Category", (metadataState as? MetadataState.Success)?.data?.category ?: emptyList(), selectedCategory, { selectedCategory = it }, { it.categoryName }, categoryExpanded, { categoryExpanded = it }, enabled = canEditCategory)
                })
            }
        }
    }
}
