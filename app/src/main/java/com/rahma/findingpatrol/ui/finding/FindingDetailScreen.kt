package com.rahma.findingpatrol.ui.finding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.rahma.findingpatrol.data.model.finding.FindingsById
import com.rahma.findingpatrol.ui.components.FullScreenImageViewer
import com.rahma.findingpatrol.util.fixImageUrl
import com.rahma.findingpatrol.viewmodel.finding.FindingDetailState
import com.rahma.findingpatrol.viewmodel.finding.FindingDetailViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FindingDetailScreen(
    viewModel: FindingDetailViewModel,
    findingId: Int,
    userRole: String,
    userMode: String, // Added userMode
    currentUserId: Int,
    sectionId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToImprovementAction: (Int) -> Unit,
    onNavigateToResolve: (Int) -> Unit,
    onNavigateToPatrolEdit: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()


    LaunchedEffect(findingId) {
        viewModel.getFindingDetail(findingId)
    }

    val (showFab, fabText, onFabClick) = when (val currentState = state) {
        is FindingDetailState.Success -> {
            val data = currentState.data
            val status = data.statusName.uppercase()
            val isCreator = data.userId == currentUserId
            val isPic = data.picId == sectionId

            when (userMode) {
                "patrol" -> {
                    if (status == "OPEN" && isCreator) {
                        Triple(true, "Edit Temuan") { onNavigateToPatrolEdit(data.findingId) }
                    } else {
                        Triple(false, "", {})
                    }
                }
                "pic" -> {
                    if ((status == "ASSIGNED" || status == "READY FOR RECHECK") && isPic) {
                        val text = if (status == "ASSIGNED") "Perbaikan Temuan" else "Edit Perbaikan"
                        Triple(true, text) { onNavigateToResolve(data.findingId) }
                    } else {
                        Triple(false, "", {})
                    }
                }
                "improvement" -> {
                    val text = when (status) {
                        "OPEN" -> "Tugaskan PIC"
                        "ASSIGNED" -> "Edit Penugasan PIC"
                        "READY FOR RECHECK" -> "Recheck Temuan"
                        "CLOSED" -> "Edit Finding"
                        else -> ""
                    }
                    if (text.isNotEmpty()) {
                        Triple(true, text) { onNavigateToImprovementAction(data.findingId) }
                    } else {
                        Triple(false, "", {})
                    }
                }
                else -> Triple(false, "", {})
            }
        }
        else -> Triple(false, "", {}) // Not success state, don't show FAB
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detail Temuan", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profil",
                        tint = Color(0xFF5E35B1),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            if (showFab) {
                ExtendedFloatingActionButton(
                    onClick = onFabClick,
                    icon = { Icon(Icons.Filled.Edit, "Edit") },
                    text = { Text(text = fabText, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
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
                    val beforeImages = data.images.filter { it.imageType == "finding" }.map { fixImageUrl(it.imageUrl) }
                    val afterImages = data.images.filter { it.imageType == "improved" }.map { fixImageUrl(it.imageUrl) }

                    var showImageViewer by remember { mutableStateOf<Pair<List<Any>, Int>?>(null) }

                    showImageViewer?.let { (images, initialIndex) ->
                        FullScreenImageViewer(
                            images = images,
                            initialIndex = initialIndex,
                            onDismiss = { showImageViewer = null },
                            onDelete = null,
                            onIndexChanged = { newIndex -> showImageViewer = images to newIndex }
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = "Temuan No. ${data.findingId}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "${data.patrolName} | Oleh: ${data.patrolCreatorName ?: "-"} ",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item { StyledChip(label = data.statusName.uppercase(), color = getStatusColor(data.statusName), textColor = getStatusTextColor(data.statusName)) }
                                data.priorityName?.let {
                                    item { StyledChip(label = "Prioritas: $it", color = getPriorityColor(it), textColor = getPriorityTextColor(it)) }
                                }
                                data.findingTypeName?.let {
                                    item { StyledChip(label = "Tipe: $it", color = getTypeColor(it), textColor = getTypeTextColor(it)) }
                                }
                                data.riskLevelName?.let {
                                    item { StyledChip(label = "Risiko: $it", color = getRiskColor(it), textColor = getRiskTextColor(it)) }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        item {
                            DetailSectionCard(
                                title = "Masalah (Sebelum Perbaikan)",
                                dotColor = Color.Red,
                                images = beforeImages,
                                onImageClick = { index ->
                                    showImageViewer = beforeImages to index
                                },
                                content = {
                                    DetailLabelValue("AREA", data.area)
                                    DetailLabelValue("DESKRIPSI TEMUAN (FINDING)", data.finding)
                                    data.countermeasureSuggestion?.let {
                                        DetailLabelValue("USULAN PERBAIKAN (COUNTERMEASURE SUGGESTION)", it)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (afterImages.isNotEmpty() || data.countermeasure != null || data.picName != null) {
                            item {
                                DetailSectionCard(
                                    title = "Solusi (Setelah Perbaikan)",
                                    dotColor = Color(0xFF4CAF50),
                                    images = afterImages,
                                    onImageClick = { index ->
                                        showImageViewer = afterImages to index
                                    },
                                    content = {
                                        data.picName?.let { DetailLabelValue("PENANGGUNG JAWAB (PIC) ", it) }
                                        data.countermeasure?.let { DetailLabelValue("TINDAKAN PERBAIKAN (COUNTERMEASURE)", it) }
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        item {
                            TimelineCard(finding = data)
                        }

                        //Spacer for button
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSectionCard(
    title: String,
    dotColor: Color,
    images: List<String>,
    onImageClick: (Int) -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(dotColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (images.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(images.size) { index ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(images[index])
                                .crossfade(true)
                                .build(),
                            contentDescription = "Gambar Temuan ${index + 1}",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onImageClick(index) },
                            contentScale = ContentScale.Crop
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
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = Color.Black
        )
    }
}

@Composable
fun StyledChip(label: String, color: Color, textColor: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color,
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = label, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatTimelineDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    return try {
        val odt = OffsetDateTime.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        odt.format(formatter)
    } catch (e: Exception) {
        dateString // Fallback
    }
}

@Composable
private fun TimelineItem(
    title: String,
    date: String,
    isLast: Boolean,
    isComplete: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        if (isComplete) Color(0xFF2E7D32) else Color.LightGray.copy(alpha = 0.5f),
                        CircleShape
                    )
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .width(1.dp)
                        .height(40.dp) // Fixed height line
                        .background(Color.LightGray)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            Text(
                text = date,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun TimelineCard(finding: FindingsById) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Column {
                Text(
                    text = "Flow Proses dan Timeline",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lead Time: ${finding.ltDay} hari",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.DarkGray
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            val timelineEvents = remember(finding) {
                listOf(
                    Triple("Ditemukan", finding.findingDate, finding.findingDate != null),
                    Triple("Ditugaskan ke PIC", finding.assignedDate, finding.assignedDate != null),
                    Triple("Selesai Diperbaiki", finding.finishDate, finding.finishDate != null),
                    Triple("Dicek Ulang & Ditutup", finding.recheckDate, finding.recheckDate != null)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                timelineEvents.forEachIndexed { index, (title, date, isComplete) ->
                    TimelineItem(
                        title = title,
                        date = date?.let { formatTimelineDate(it) } ?: "-",
                        isLast = index == timelineEvents.lastIndex,
                        isComplete = isComplete
                    )
                }
            }
        }
    }
}

fun getStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "OPEN" -> Color(0xFFE3F2FD)
        "ASSIGNED" -> Color(0xFFFFF9C4)
        "READY FOR RECHECK" -> Color(0xFFFFE0B2)
        "CLOSED" -> Color(0xFFC8E6C9)
        else -> Color.Gray
    }
}

fun getStatusTextColor(status: String): Color {
    return when (status.uppercase()) {
        "OPEN" -> Color(0xFF0D47A1)
        "ASSIGNED" -> Color(0xFFF57F17)
        "READY FOR RECHECK" -> Color(0xFFE65100)
        "CLOSED" -> Color(0xFF1B5E20)
        else -> Color.White
    }
}

fun getPriorityColor(priority: String): Color {
    return when (priority.uppercase()) {
        "LOW" -> Color(0xFFE0F7FA)
        "MEDIUM" -> Color(0xFFFFFDE7)
        "HIGH" -> Color(0xFFFBE9E7)
        else -> Color.Gray
    }
}

fun getPriorityTextColor(priority: String): Color {
    return when (priority.uppercase()) {
        "LOW" -> Color(0xFF006064)
        "MEDIUM" -> Color(0xFFF57F17)
        "HIGH" -> Color(0xFFBF360C)
        else -> Color.White
    }
}

fun getTypeColor(type: String): Color {
    return when (type.uppercase()) {
        "INITIAL" -> Color(0xFFE3F2FD)
        "RECURRENCE" -> Color(0xFFFFF9C4)
        "SIMILAR" -> Color(0xFFFFE0B2)
        else -> Color(0xFFF5F5F5)
    }
}

fun getTypeTextColor(type: String): Color {
    return when (type.uppercase()) {
        "INITIAL" -> Color(0xFF0D47A1)
        "RECURRENCE" -> Color(0xFFF57F17)
        "SIMILAR" -> Color(0xFFE65100)
        else -> Color.Black
    }
}

fun getRiskColor(risk: String): Color {
    return when (risk.uppercase()) {
        "LEVEL 1" -> Color(0xFFE0F7FA)
        "LEVEL 2" -> Color(0xFFFFFDE7)
        "LEVEL 3" -> Color(0xFFFFE0B2)
        "LEVEL 4" -> Color(0xFFFBE9E7)
        else -> Color.Gray
    }
}

fun getRiskTextColor(risk: String): Color {
    return when (risk.uppercase()) {
        "LEVEL 1" -> Color(0xFF006064)
        "LEVEL 2" -> Color(0xFFF57F17)
        "LEVEL 3" -> Color(0xFFE65100)
        "LEVEL 4" -> Color(0xFFBF360C)
        else -> Color.White
    }
}
