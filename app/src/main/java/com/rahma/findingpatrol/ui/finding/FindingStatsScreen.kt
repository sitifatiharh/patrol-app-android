package com.rahma.findingpatrol.ui.finding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rahma.findingpatrol.data.model.metadata.Category
import com.rahma.findingpatrol.data.model.metadata.Patrol
import com.rahma.findingpatrol.viewmodel.finding.FindingHistoryViewModel
import com.rahma.findingpatrol.viewmodel.finding.HistoryState
import com.rahma.findingpatrol.viewmodel.finding.filterAndSortFindings
import java.time.Month
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindingStatsScreen(
    viewModel: FindingHistoryViewModel,
    initialStatusId: Int,
    initialStatusName: String,
    userId: Int,
    userRole: String,
    userMode: String,
    sectionId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val historyState by viewModel.historyState.collectAsState()
    val patrols by viewModel.patrols.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val isClosedStatus = initialStatusId == 4

    var selectedPatrol by remember { mutableStateOf<Patrol?>(null) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    val currentCalendar = Calendar.getInstance()
    var selectedYear by remember { mutableIntStateOf(currentCalendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(currentCalendar.get(Calendar.MONTH) + 1) }
    var showMonthYearPicker by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    var currentPage by remember { mutableIntStateOf(0) }
    val itemsPerPage = 4

    val date: String? = if (isClosedStatus) {
        "$selectedYear-${String.format("%02d", selectedMonth)}"
    } else {
        null
    }

    LaunchedEffect(
        initialStatusId,
        selectedPatrol,
        selectedCategory,
        selectedYear,
        selectedMonth,
        userRole,
        userMode
    ) {
        viewModel.loadFindingsForStats(
            userId = userId,
            userMode = userMode,
            sectionId = sectionId,
            statusId = initialStatusId,
            patrolId = selectedPatrol?.patrolId,
            date = date
        )

        if (patrols.isEmpty()) viewModel.loadPatrols()
        if (isClosedStatus && categories.isEmpty()) viewModel.loadCategories()
    }

    LaunchedEffect(selectedPatrol, selectedCategory, selectedMonth, selectedYear, userRole, userMode) {
        currentPage = 0
    }

    if (isClosedStatus && showFilterDialog) {
        StatsFilterDialog(
            patrols = patrols,
            categories = categories,
            selectedPatrol = selectedPatrol,
            selectedCategory = selectedCategory,
            onPatrolSelected = { selectedPatrol = it },
            onCategorySelected = { selectedCategory = it },
            onDismiss = { showFilterDialog = false }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Temuan: $initialStatusName",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isClosedStatus) {
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                        IconButton(onClick = { showMonthYearPicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pilih Bulan")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showMonthYearPicker) {
            MonthYearPickerDialog(
                initialYear = selectedYear,
                initialMonth = selectedMonth,
                onDismiss = { showMonthYearPicker = false },
                onMonthSelected = { year, month ->
                    selectedYear = year
                    selectedMonth = month
                    showMonthYearPicker = false
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (isClosedStatus) {
                Text(
                    text = "${Month.of(selectedMonth).getDisplayName(TextStyle.FULL, Locale.getDefault())} $selectedYear",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            when (val state = historyState) {
                is HistoryState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is HistoryState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                is HistoryState.Success -> {
                    val sortedList = filterAndSortFindings(state.data, "Prioritas")

                    if (sortedList.isEmpty()) {
                        Text(
                            text = "Tidak ada temuan dengan filter ini.",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        val totalItems = sortedList.size
                        val totalPages = (totalItems + itemsPerPage - 1) / itemsPerPage

                        if (currentPage >= totalPages && totalPages > 0) {
                            currentPage = totalPages - 1
                        }

                        val startIndex = currentPage * itemsPerPage
                        val endIndex = min(startIndex + itemsPerPage, totalItems)
                        val pageItems = if (startIndex < totalItems) sortedList.subList(startIndex, endIndex) else emptyList()

                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(pageItems) { finding ->
                                    FindingCard(
                                        data = finding.toCardData(),
                                        onClick = { onNavigateToDetail(finding.findingId) }
                                    )
                                }
                            }

                            if (totalPages > 1) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { if (currentPage > 0) currentPage-- },
                                        enabled = currentPage > 0
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                                    }

                                    Text(
                                        text = "Page ${currentPage + 1} of $totalPages",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    IconButton(
                                        onClick = { if (currentPage < totalPages - 1) currentPage++ },
                                        enabled = currentPage < totalPages - 1
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
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
fun StatsFilterDialog(
    patrols: List<Patrol>,
    categories: List<Category>,
    selectedPatrol: Patrol?,
    selectedCategory: Category?,
    onPatrolSelected: (Patrol?) -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Filter Temuan", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                FilterGroup(
                    title = "Patrol",
                    items = patrols,
                    selectedItem = selectedPatrol,
                    onItemSelected = onPatrolSelected,
                    getItemName = { it.patrolName }
                )
                FilterGroup(
                    title = "Kategori",
                    items = categories,
                    selectedItem = selectedCategory,
                    onItemSelected = onCategorySelected,
                    getItemName = { it.categoryName }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FilterGroup(
    title: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T?) -> Unit,
    getItemName: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.padding(vertical = 8.dp)) {
        Text(title, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedItem?.let(getItemName) ?: "Semua",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle dropdown"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("Semua") }, onClick = {
                    onItemSelected(null)
                    expanded = false
                })
                items.forEach {
                    DropdownMenuItem(text = { Text(getItemName(it)) }, onClick = {
                        onItemSelected(it)
                        expanded = false
                    })
                }
            }
        }
    }
}

@Composable
private fun MonthYearPickerDialog(
    initialYear: Int,
    initialMonth: Int, // 1-based
    onDismiss: () -> Unit,
    onMonthSelected: (year: Int, month: Int) -> Unit
) {
    var year by remember { mutableIntStateOf(initialYear) }
    val months = java.time.Month.values().map { it.getDisplayName(TextStyle.FULL, Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { year-- }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tahun Sebelumnya")
                }
                Text(text = year.toString(), fontWeight = FontWeight.Bold)
                IconButton(onClick = { year++ }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Tahun Berikutnya")
                }
            }
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(months.size) { index ->
                    val monthNumber = index + 1
                    val isSelected = year == initialYear && monthNumber == initialMonth
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onMonthSelected(year, monthNumber) }
                    ) {
                        Text(
                            text = months[index].take(3),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
