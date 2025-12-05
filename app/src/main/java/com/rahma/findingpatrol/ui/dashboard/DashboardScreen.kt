package com.rahma.findingpatrol.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahma.findingpatrol.data.model.dashboard.SummaryData
import com.rahma.findingpatrol.viewmodel.dashboard.DashboardState
import com.rahma.findingpatrol.viewmodel.dashboard.DashboardViewModel
import com.rahma.findingpatrol.viewmodel.dashboard.HistoryState
import com.rahma.findingpatrol.ui.finding.FindingCard
import com.rahma.findingpatrol.ui.finding.toCardData

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToFindings: () -> Unit,
    onNavigateToCreateFinding: () -> Unit
) {
    val summaryState by viewModel.summaryState.collectAsState()
    val historyState by viewModel.historyState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top right profile
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.LightGray, CircleShape)
                    .clickable { onNavigateToProfile() },
                contentAlignment = Alignment.Center
            ) {
                Text("U", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // My Patrol card (main container)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("History", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("see more >", color = Color.Blue, modifier = Modifier.clickable { onNavigateToFindings() })
                }
                Spacer(modifier = Modifier.height(10.dp))

                // history preview items inside main card
                when (historyState) {
                    is HistoryState.Loading -> Text("Loading history...")
                    is HistoryState.Error -> Text((historyState as HistoryState.Error).message)
                    is HistoryState.Success -> {
                        val items = (historyState as HistoryState.Success).data
                        Column {
                            items.take(2).forEach { finding ->
                                FindingCard(
                                    data = finding.toCardData(),
                                    onClick = { /* navigate to detail */ }
                                )
                            }
                        }
                    }



                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary card with 4 boxes
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Findings", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))

                when (summaryState) {
                    is DashboardState.Loading -> Text("Loading summary...")
                    is DashboardState.Error -> Text((summaryState as DashboardState.Error).message)
                    is DashboardState.Success -> {
                        val summary: SummaryData = (summaryState as DashboardState.Success).data
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(17.dp)) {
                            SmallStatBox("Open", summary.open.toString())
                            SmallStatBox("Assigned", summary.assigned.toString())
                            SmallStatBox("For Recheck", summary.readyForRecheck.toString())
                            SmallStatBox("Closed", summary.closed.month.toString())
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Call-to-action card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToCreateFinding() },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Benahi / Perbaiki apa yang mengganggumu!!!", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Ayo Laporkan buat perbaikan", fontSize = 13.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = { onNavigateToCreateFinding() }, modifier = Modifier.align(Alignment.Start)) {
                    Text("Buat Laporan")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom home button (center)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.size(width = 120.dp, height = 56.dp), elevation = CardDefaults.cardElevation(6.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("HOME", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SmallStatBox(label: String, count: String) {
    Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.size(width = 72.dp, height = 72.dp), elevation = CardDefaults.cardElevation(3.dp)) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(count, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, fontSize = 12.sp)
        }
    }
}
