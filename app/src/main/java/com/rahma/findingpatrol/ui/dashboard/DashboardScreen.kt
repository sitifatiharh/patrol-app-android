package com.rahma.findingpatrol.ui.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahma.findingpatrol.data.model.dashboard.SummaryData
import com.rahma.findingpatrol.ui.finding.FindingCard
import com.rahma.findingpatrol.ui.finding.toCardData
import com.rahma.findingpatrol.util.isImprovementRole
import com.rahma.findingpatrol.util.isPicRole
import com.rahma.findingpatrol.viewmodel.dashboard.DashboardViewModel
import com.rahma.findingpatrol.viewmodel.dashboard.DashboardState
import com.rahma.findingpatrol.viewmodel.dashboard.HistoryState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    userId: Int,
    userRole: String,
    userMode: String,
    sectionId: Int,
    sectionName: String, // Pastikan parameter ini diteruskan dari NavGraph
    onNavigateToProfile: () -> Unit,
    onNavigateToFindings: () -> Unit,
    onNavigateToCreateFinding: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToDebug: () -> Unit,
    onNavigateToFindingStats: (statusId: Int, statusName: String, userMode: String) -> Unit // Pass mode on navigation
) {
    val summaryState by viewModel.summaryState.collectAsState()
    val historyState by viewModel.historyState.collectAsState()

    LaunchedEffect(userId, userRole, userMode, sectionId) {
        if (userId != -1 && userRole.isNotEmpty() && sectionId != -1) {
            viewModel.loadDashboard(userId, sectionId, userMode)
        }
    }

    val currentMonth = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID")))
    }

    val findingsTitle = "History"

    Scaffold(
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateToDebug) {
                        Icon(Icons.Default.BugReport, "Debug", tint = Color.Red)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = {
                                val newMode = if (userMode == "patrol") {
                                    when {
                                        isPicRole(userRole) -> "pic"
                                        isImprovementRole(userRole) -> "improvement"
                                        else -> "patrol" // Kembali ke patrol jika role tidak sesuai
                                    }
                                } else {
                                    "patrol" // Dari mode lain, selalu kembali ke patrol
                                }
                                viewModel.saveUserMode(newMode)
                            },
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Ganti Mode")
                            Spacer(modifier = Modifier.width(8.dp))
                            val buttonText = when (userMode) {
                                "patrol" -> "Patrol"
                                "pic" -> "PIC: $sectionName"
                                "improvement" -> "Improvement"
                                else -> userMode.replaceFirstChar { it.uppercase() }
                            }
                            Text(buttonText)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color(0xFF5E35B1),
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onNavigateToProfile() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Findings", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(currentMonth, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        when (summaryState) {
                            is DashboardState.Loading -> Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                            is DashboardState.Error -> Text((summaryState as DashboardState.Error).message, fontSize = 17.sp)
                            is DashboardState.Success -> {
                                val summary: SummaryData = (summaryState as DashboardState.Success).data

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    StatItem(
                                        modifier = Modifier.weight(1f),
                                        count = summary.open.toString(),
                                        label = "Open",
                                        enabled = userMode != "pic"
                                    ) { onNavigateToFindingStats(1, "Open", userMode) }
                                    StatItem(
                                        modifier = Modifier.weight(1f),
                                        count = summary.assigned.toString(),
                                        label = "Assigned"
                                    ) { onNavigateToFindingStats(2, "Assigned", userMode) }
                                    StatItem(
                                        modifier = Modifier.weight(1f),
                                        count = summary.readyForRecheck.toString(),
                                        label = "Ready for Recheck"
                                    ) { onNavigateToFindingStats(3, "Ready for Recheck", userMode) }
                                    val closedText = "${summary.closed.today}/${summary.closed.month}"
                                    StatItem(
                                        modifier = Modifier.weight(1f),
                                        count = closedText,
                                        label = "Closed"
                                    ) { onNavigateToFindingStats(4, "Closed", userMode) }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToCreateFinding() },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Benahi / Perbaiki apa yang mengganggumu!!!", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Ayo Laporkan buat perbaikan", fontSize = 16.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = { onNavigateToCreateFinding() }, modifier = Modifier.align(Alignment.Start)) {
                            Text("Buat Laporan", fontSize = 17.sp)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(findingsTitle, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = Color.Black)

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "See more",
                                tint = Color.Black,
                                modifier = Modifier
                                    .clickable { onNavigateToFindings() }
                                    .size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        when (historyState) {
                            is HistoryState.Loading -> Text("Loading findings...", fontSize = 17.sp)
                            is HistoryState.Error -> Text((historyState as HistoryState.Error).message, fontSize = 17.sp)
                            is HistoryState.Success -> {
                                val items = (historyState as HistoryState.Success).data
                                Column {
                                    items.take(2).forEach { finding ->
                                        FindingCard(
                                            data = finding.toCardData(),
                                            onClick = { onNavigateToDetail(finding.findingId) }
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

@Composable
fun StatItem(
    modifier: Modifier = Modifier,
    count: String,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val color = if (enabled) Color.Black else Color.Gray
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 4.dp) // Add some horizontal padding between items
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp) // Fixed height for uniform boxes
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, if (enabled) Color.LightGray else Color.Transparent, RoundedCornerShape(8.dp))
                .background(if (enabled) Color.White else Color(0xFFF0F0F0))
        ) {
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = color,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
