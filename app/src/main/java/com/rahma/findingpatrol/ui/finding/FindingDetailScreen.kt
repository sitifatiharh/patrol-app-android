package com.rahma.findingpatrol.ui.finding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rahma.findingpatrol.data.model.finding.FindingsById
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
                        val text = if (status == "ASSIGNED") "Kerjakan Temuan" else "Edit Perbaikan"
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

                    var showImageDialog by remember { mutableStateOf(false) }
                    var initialDialogImageIndex by remember { mutableStateOf(0) }
                    var dialogImages by remember { mutableStateOf<List<String>>(emptyList()) }

                    if (showImageDialog) {
                        FullScreenImageViewer(
                            images = dialogImages,
                            initialIndex = initialDialogImageIndex,
                            onDismiss = { showImageDialog = false }
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
                                    dialogImages = beforeImages
                                    initialDialogImageIndex = index
                                    showImageDialog = true
                                },
                                content = {
                                    DetailLabelValue("AREA", data.area)
                                    DetailLabelValue("DESKRIPSI TEMUAN", data.finding)
                                    data.countermeasureSuggestion?.let {
                                        DetailLabelValue("USULAN PERBAIKAN", it)
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
                                        dialogImages = afterImages
                                        initialDialogImageIndex = index
                                        showImageDialog = true
                                    },
                                    content = {
                                        data.picName?.let { DetailLabelValue("PENANGGUNG JAWAB", it) }
                                        data.countermeasure?.let { DetailLabelValue("TINDAKAN PERBAIKAN", it) }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenImageViewer(images: List<String>, initialIndex: Int, onDismiss: () -> Unit) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { images.size }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.9f)) {
            Box(contentAlignment = Alignment.Center) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
                        scale = (scale * zoomChange).coerceIn(1f, 5f)
                        val newOffsetX = offset.x + panChange.x * scale
                        val newOffsetY = offset.y + panChange.y * scale
                        val maxX = (scale - 1) * 500 // Approximate bounds
                        val maxY = (scale - 1) * 500
                        offset = Offset(newOffsetX.coerceIn(-maxX, maxX), newOffsetY.coerceIn(-maxY, maxY))
                    }

                    AsyncImage(
                        model = images[page],
                        contentDescription = "Gambar Penuh ${page + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            .transformable(state = transformableState)
                            .clickable {
                                // Reset zoom and pan on simple click
                                scale = 1f
                                offset = Offset.Zero
                            }
                    )
                }

                // Close Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                }

                // Indicator
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)

                ) {
                    repeat(images.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) Color.White else Color.Gray
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(10.dp)
                        )
                    }
                }
            }
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
    isLast: Boolean
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
                    .background(Color(0xFF2E7D32), CircleShape)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Flow Proses dan Timeline",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Lead Time: ${finding.ltDay} hari",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.DarkGray
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            val timelineEvents = remember(finding) {
                listOfNotNull(
                    "Ditemukan" to finding.findingDate,
                    finding.assignedDate?.let { "Ditugaskan ke PIC" to it },
                    finding.finishDate?.let { "Selesai Diperbaiki" to it },
                    finding.recheckDate?.let { "Dicek Ulang & Ditutup" to it }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                timelineEvents.forEachIndexed { index, (title, date) ->
                    TimelineItem(
                        title = title,
                        date = formatTimelineDate(date),
                        isLast = index == timelineEvents.lastIndex
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
        "RENDAH" -> Color(0xFFE0F7FA)
        "SEDANG" -> Color(0xFFFFFDE7)
        "TINGGI" -> Color(0xFFFBE9E7)
        else -> Color.Gray
    }
}

fun getPriorityTextColor(priority: String): Color {
    return when (priority.uppercase()) {
        "RENDAH" -> Color(0xFF006064)
        "SEDANG" -> Color(0xFFF57F17)
        "TINGGI" -> Color(0xFFBF360C)
        else -> Color.White
    }
}

fun getTypeColor(type: String): Color {
    return when (type.uppercase()) {
        "AMAN" -> Color(0xFFE8F5E9)
        "TIDAK AMAN" -> Color(0xFFFFF8E1)
        else -> Color(0xFFF5F5F5)
    }
}

fun getTypeTextColor(type: String): Color {
    return when (type.uppercase()) {
        "AMAN" -> Color(0xFF2E7D32)
        "TIDAK AMAN" -> Color(0xFFF57F17)
        else -> Color.Black
    }
}

fun getRiskColor(risk: String): Color {
    return when (risk.uppercase()) {
        "RENDAH" -> Color(0xFFE0F7FA)
        "SEDANG" -> Color(0xFFFFFDE7)
        "TINGGI" -> Color(0xFFFBE9E7)
        else -> Color.Gray
    }
}

fun getRiskTextColor(risk: String): Color {
    return when (risk.uppercase()) {
        "RENDAH" -> Color(0xFF006064)
        "SEDANG" -> Color(0xFFF57F17)
        "TINGGI" -> Color(0xFFBF360C)
        else -> Color.White
    }
}
