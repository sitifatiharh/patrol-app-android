package com.rahma.findingpatrol.ui.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahma.findingpatrol.R
import com.rahma.findingpatrol.data.model.dashboard.SummaryData
import com.rahma.findingpatrol.viewmodel.dashboard.DashboardState
import com.rahma.findingpatrol.viewmodel.dashboard.DashboardViewModel
import com.rahma.findingpatrol.viewmodel.dashboard.HistoryState
import com.rahma.findingpatrol.ui.finding.FindingCard
import com.rahma.findingpatrol.ui.finding.toCardData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


@SuppressLint("NewApi")
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToFindings: () -> Unit,
    onNavigateToCreateFinding: () -> Unit,
    onNavigateToDetail: (Int) -> Unit // Added callback
) {
    val summaryState by viewModel.summaryState.collectAsState()
    val historyState by viewModel.historyState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    val currentMonth = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("id-ID")))
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp) 
        ) {
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

            // My Patrol card (History)
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
                        Text("History", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        
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

                    // history preview items
                    when (historyState) {
                        is HistoryState.Loading -> Text("Loading history...")
                        is HistoryState.Error -> Text((historyState as HistoryState.Error).message)
                        is HistoryState.Success -> {
                            val items = (historyState as HistoryState.Success).data
                            Column {
                                items.take(2).forEach { finding ->
                                    FindingCard(
                                        data = finding.toCardData(),
                                        onClick = { onNavigateToDetail(finding.findingId) } // Pass ID
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary card
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
                        Text("Findings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(currentMonth, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    when (summaryState) {
                        is DashboardState.Loading -> Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        is DashboardState.Error -> Text((summaryState as DashboardState.Error).message)
                        is DashboardState.Success -> {
                            val summary: SummaryData = (summaryState as DashboardState.Success).data
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatItem(count = summary.open.toString(), label = "New")
                                StatItem(count = summary.assigned.toString(), label = "Assigned")
                                StatItem(count = summary.readyForRecheck.toString(), label = "Ready fo Rechek")
                                val closedText = "${summary.closed.today}/${summary.closed.month}"
                                StatItem(count = closedText, label = "Closed")
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
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Benahi / Perbaiki apa yang mengganggumu!!!", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Ayo Laporkan buat perbaikan", fontSize = 13.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { onNavigateToCreateFinding() }, modifier = Modifier.align(Alignment.Start)) {
                        Text("Buat Laporan")
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .size(88.dp)
                .clip(CircleShape)
                .background(Color(0xFFECEAF0)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "Home",
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun StatItem(count: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .size(72.dp)
                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Black,
            lineHeight = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
