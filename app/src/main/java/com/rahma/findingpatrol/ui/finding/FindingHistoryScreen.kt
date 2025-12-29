package com.rahma.findingpatrol.ui.finding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahma.findingpatrol.data.model.metadata.Category
import com.rahma.findingpatrol.data.model.metadata.Patrol
import com.rahma.findingpatrol.data.model.metadata.Priority
import com.rahma.findingpatrol.data.model.metadata.Status
import com.rahma.findingpatrol.viewmodel.finding.FindingHistoryViewModel
import com.rahma.findingpatrol.viewmodel.finding.HistoryState
import com.rahma.findingpatrol.viewmodel.finding.filterAndSortFindings
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindingHistoryScreen(
    viewModel: FindingHistoryViewModel,
    userId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val historyState by viewModel.historyState.collectAsState()
    val patrols by viewModel.patrols.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val priorities by viewModel.priorities.collectAsState()
    val statuses by viewModel.statuses.collectAsState()

    var selectedPatrol by remember { mutableStateOf<Patrol?>(null) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedPriority by remember { mutableStateOf<Priority?>(null) }
    var selectedStatus by remember { mutableStateOf<Status?>(null) }

    var currentPage by remember { mutableIntStateOf(0) }
    val itemsPerPage = 4

    var showPatrolMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    LaunchedEffect(selectedPatrol, selectedCategory, selectedPriority, selectedStatus) {
        viewModel.loadFindings(
            mode = "patrol",
            userId = userId, // Hardcoded to patrol mode as requested
            patrolId = selectedPatrol?.patrolId,
            statusId = selectedStatus?.statusId,
            priorityId = selectedPriority?.priorityId,
            categoryId = selectedCategory?.categoryId
        )
        currentPage = 0
    }

    LaunchedEffect(Unit) {
        viewModel.loadPatrols()
        viewModel.loadCategories()
        viewModel.loadPriorities()
        viewModel.loadStatuses()
    }

    val title = "History"

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Filter Bar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                // Patrol Filter
                FilterDropdown(items = patrols, selectedItem = selectedPatrol, onItemSelected = { selectedPatrol = it }, showMenu = showPatrolMenu, onMenuToggle = { showPatrolMenu = it }, defaultText = "Patrol")
                // Status Filter
                FilterDropdown(items = statuses, selectedItem = selectedStatus, onItemSelected = { selectedStatus = it }, showMenu = showStatusMenu, onMenuToggle = { showStatusMenu = it }, defaultText = "Status")
                // Priority Filter
                FilterDropdown(items = priorities, selectedItem = selectedPriority, onItemSelected = { selectedPriority = it }, showMenu = showPriorityMenu, onMenuToggle = { showPriorityMenu = it }, defaultText = "Priority")
                // Category Filter
                FilterDropdown(items = categories, selectedItem = selectedCategory, onItemSelected = { selectedCategory = it }, showMenu = showCategoryMenu, onMenuToggle = { showCategoryMenu = it }, defaultText = "Category")
            }

            Spacer(modifier = Modifier.padding(8.dp))

            when (val state = historyState) {
                is HistoryState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is HistoryState.Error -> {
                    Text(text = state.message, color = Color.Red)
                }
                is HistoryState.Success -> {
                    val sortedList = filterAndSortFindings(state.data, "Tanggal")

                    if (sortedList.isEmpty()) {
                        Text(
                            text = "Tidak ada riwayat temuan.",
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
fun <T> FilterDropdown(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T?) -> Unit,
    showMenu: Boolean,
    onMenuToggle: (Boolean) -> Unit,
    defaultText: String
) {
    Box {
        TextButton(onClick = { onMenuToggle(true) }) {
            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.Black)
            Spacer(modifier = Modifier.width(4.dp))
            val text = when (selectedItem) {
                is Patrol -> selectedItem.patrolName
                is Status -> selectedItem.statusName
                is Priority -> selectedItem.priorityName
                is Category -> selectedItem.categoryName
                else -> defaultText
            }
            Text(text, color = Color.Black)
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { onMenuToggle(false) }
        ) {
            DropdownMenuItem(
                text = { Text("Semua") },
                onClick = {
                    onItemSelected(null)
                    onMenuToggle(false)
                }
            )
            items.forEach { item ->
                val name = when (item) {
                    is Patrol -> item.patrolName
                    is Status -> item.statusName
                    is Priority -> item.priorityName
                    is Category -> item.categoryName
                    else -> ""
                }
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onItemSelected(item)
                        onMenuToggle(false)
                    }
                )
            }
        }
    }
}
