package com.rahma.findingpatrol.ui.finding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rahma.findingpatrol.viewmodel.finding.FindingDetailState
import com.rahma.findingpatrol.viewmodel.finding.FindingDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindingDetailScreen(
    viewModel: FindingDetailViewModel,
    findingId: Int,
    onNavigateBack: () -> Unit,
    // Callback Navigasi
    onNavigateToImprovementAction: (Int) -> Unit, // Form 3, 5, 6 (Improvement Super Screen)
    onNavigateToResolve: (Int) -> Unit, // Form 4 (PIC Resolve)
    onNavigateToPatrolEdit: (Int) -> Unit, // Form 2 (Patrol/PIC Edit Open)
    // User Context
    userRole: String = "IMPROVEMENT", 
    currentUserId: Int = 2
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(findingId) {
        viewModel.getFindingDetail(findingId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detail", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                     Icon(
                         imageVector = Icons.Default.Person, 
                         contentDescription = "Profile",
                         tint = Color(0xFF5E35B1),
                         modifier = Modifier.padding(end = 16.dp)
                     )
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val currentState = state) {
                is FindingDetailState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is FindingDetailState.Error -> {
                    Text(
                        text = currentState.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is FindingDetailState.Success -> {
                    val data = currentState.data
                    val status = data.statusName.uppercase()
                    
                    // --- Logic Tombol Edit Tunggal ---
                    val onEditClick = {
                        if (userRole == "IMPROVEMENT") {
                            // Improvement User -> Always to Super Screen (handles all logic internally)
                            onNavigateToImprovementAction(data.findingId)
                        } else if (userRole == "PIC") {
                            if (status == "ASSIGNED") {
                                onNavigateToResolve(data.findingId) // Form 4
                            } else if (status == "OPEN") {
                                onNavigateToPatrolEdit(data.findingId) // Form 2
                            }
                        } else {
                            // Patrol / Others
                            if (status == "OPEN") {
                                onNavigateToPatrolEdit(data.findingId) // Form 2
                            }
                        }
                    }
                    
                    // Show Edit Button Logic:
                    // Hide if Patrol/PIC on Closed/Ready? Usually View Only.
                    // Improvement always sees Edit.
                    val showEditButton = when(userRole) {
                        "IMPROVEMENT" -> true
                        "PIC" -> status == "OPEN" || status == "ASSIGNED"
                        else -> status == "OPEN" // Patrol only Edit if Open
                    }

                    // Filter Images
                    val beforeImages = data.images.filter { it.imageType == "finding" }.map { it.imageUrl }
                    val afterImages = data.images.filter { it.imageType == "improved" }.map { it.imageUrl }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // 1. Header Section
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "Finding No. ${data.findingId}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "Safety Patrol | By: ${data.patrolCreatorName ?: "-"}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                
                                // Smart Edit Button
                                if (showEditButton) {
                                    IconButton(
                                        onClick = onEditClick,
                                        modifier = Modifier
                                            .background(Color(0xFFF5F5F5), CircleShape)
                                            .size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Tags Row
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatusChip(label = data.statusName.uppercase(), color = getStatusColor(data.statusName), textColor = getStatusTextColor(data.statusName))
                                
                                if (data.priorityName != null) {
                                    StatusChip(label = data.priorityName.uppercase(), color = Color(0xFFF5F5F5), textColor = Color.Black)
                                }
                                
                                if (data.findingTypeName != null) {
                                    StatusChip(label = "Type: ${data.findingTypeName}", color = Color.White, textColor = Color.Black, border = true)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                if (data.riskLevelName != null) {
                                    StatusChip(label = "Risk: ${data.riskLevelName}", color = Color.White, textColor = Color.Black, border = true)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // 2. Section: Problem (Before)
                        item {
                            DetailSectionCard(
                                title = "Problem (Before)",
                                dotColor = Color.Red,
                                images = beforeImages, 
                                content = {
                                    DetailLabelValue("AREA", data.area)
                                    DetailLabelValue("FINDING DESCRIPTION", data.finding)
                                    if (data.countermeasureSuggestion != null) {
                                        DetailLabelValue("IMPROVEMENT PROPOSAL", data.countermeasureSuggestion)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // 3. Section: Solution (After)
                        if (afterImages.isNotEmpty() || data.countermeasure != null || data.picName != null) {
                            item {
                                DetailSectionCard(
                                    title = "Solution (After)",
                                    dotColor = Color(0xFF4CAF50),
                                    images = afterImages,
                                    content = {
                                        DetailLabelValue("PIC", data.picName ?: "-")
                                        DetailLabelValue("CORRECTIVE ACTION", data.countermeasure ?: "-")
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // 4. Timeline
                        item {
                            Text("Timeline", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            TimelineStep(
                                title = "Finding Created", 
                                date = formatTimelineDate(data.findingDate), 
                                isCompleted = true,
                                isFirst = true
                            )
                            TimelineStep(
                                title = "Assigned to PIC", 
                                date = formatTimelineDate(data.assignedDate), 
                                isCompleted = data.assignedDate != null,
                            )
                            TimelineStep(
                                title = "Fixed by PIC", 
                                date = formatTimelineDate(data.finishDate), 
                                isCompleted = data.finishDate != null,
                            )
                            TimelineStep(
                                title = "Rechecked & Closed", 
                                date = formatTimelineDate(data.recheckDate), 
                                isCompleted = data.recheckDate != null,
                                isLast = true
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

// --- Helper Functions & Components ---

fun formatTimelineDate(dateString: String?): String {
    if (dateString == null) return "-"
    return try {
        dateString.substring(0, 10)
    } catch (e: Exception) {
        dateString
    }
}

fun fixImageUrl(localPath: String): String {
    val normalizedPath = localPath.replace("\\", "/")
    val relativePath = normalizedPath.substringAfter("patrol-api/")
    return "http://192.17.102.221:3000/kaizen/$relativePath"
}

fun getStatusColor(status: String): Color {
    return when(status.uppercase()) {
        "OPEN" -> Color(0xFFFFEBEE)
        "ASSIGNED" -> Color(0xFFFFF3E0)
        "READY FOR RECHECK" -> Color(0xFFE3F2FD)
        "CLOSED" -> Color(0xFFE8F5E9)
        else -> Color.Gray
    }
}

fun getStatusTextColor(status: String): Color {
    return when(status.uppercase()) {
        "OPEN" -> Color(0xFFD32F2F)
        "ASSIGNED" -> Color(0xFFF57C00)
        "READY FOR RECHECK" -> Color(0xFF1976D2)
        "CLOSED" -> Color(0xFF4CAF50)
        else -> Color.White
    }
}

@Composable
fun StatusChip(label: String, color: Color, textColor: Color, border: Boolean = false) {
    Surface(
        color = color,
        shape = RoundedCornerShape(8.dp),
        border = if (border) androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray) else null,
        modifier = Modifier.height(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun DetailSectionCard(
    title: String,
    dotColor: Color,
    images: List<String>,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(dotColor, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (images.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                ) {
                    items(images) { localPath ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(fixImageUrl(localPath))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Evidence Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(280.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF5F5F5))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            content()
        }
    }
}

@Composable
fun DetailLabelValue(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(text = label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 14.sp, color = Color.Black)
    }
}

@Composable
fun TimelineStep(
    title: String, 
    date: String, 
    isCompleted: Boolean, 
    isFirst: Boolean = false, 
    isLast: Boolean = false
) {
    val dotColor = if (isCompleted) Color(0xFF00897B) else Color.LightGray 
    
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            if (!isFirst) {
                Box(modifier = Modifier.width(2.dp).weight(1f).background(if (isCompleted) dotColor else Color.LightGray))
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(dotColor, CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            )
            
            if (!isLast) {
                Box(modifier = Modifier.width(2.dp).weight(1f).background(if (isCompleted) dotColor else Color.LightGray))
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isCompleted) Color.Black else Color.Gray)
            Text(text = date, fontSize = 12.sp, color = Color.Gray)
        }
    }
}
