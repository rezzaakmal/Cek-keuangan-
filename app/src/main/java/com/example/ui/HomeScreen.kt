package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CategoryBreakdownView
import com.example.ui.components.SummaryHeader
import com.example.ui.components.TransactionFormDialog
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val monthTransactions by viewModel.monthTransactions.collectAsStateWithLifecycle()
    val displayedTransactions by viewModel.displayedTransactions.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val totalBalance by viewModel.totalBalance.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()

    val isFormOpen by viewModel.isFormOpen.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    // Listen to snackbar messages
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Pencatat Keuangan Mandiri",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Catat Kas, Pemasukan & Pengeluaran",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddForm() },
                modifier = Modifier.testTag("add_transaction_fab"),
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Transaksi"
                    )
                },
                text = {
                    Text(
                        text = "Tambah Transaksi",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("main_transaction_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Card with Month Filter and Excel Export Button
            item {
                SummaryHeader(
                    selectedMonth = selectedMonth,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    totalBalance = totalBalance,
                    onPreviousMonth = { viewModel.setPreviousMonth() },
                    onNextMonth = { viewModel.setNextMonth() },
                    onExportExcel = { viewModel.exportToExcel(context) }
                )
            }

            // Expense Distribution Breakdown
            item {
                CategoryBreakdownView(transactions = monthTransactions)
            }

            // Search Bar & Filter Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search Text Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Cari kategori, catatan, dll...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Cari"
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus Pencarian")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input")
                    )

                    // Type Filter Chips: Semua, Pemasukan, Pengeluaran
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = typeFilter == TypeFilter.ALL,
                            onClick = { viewModel.setTypeFilter(TypeFilter.ALL) },
                            label = { Text("Semua") },
                            modifier = Modifier.testTag("filter_all")
                        )
                        FilterChip(
                            selected = typeFilter == TypeFilter.INCOME,
                            onClick = { viewModel.setTypeFilter(TypeFilter.INCOME) },
                            label = { Text("Pemasukan (+)") },
                            modifier = Modifier.testTag("filter_income"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        FilterChip(
                            selected = typeFilter == TypeFilter.EXPENSE,
                            onClick = { viewModel.setTypeFilter(TypeFilter.EXPENSE) },
                            label = { Text("Pengeluaran (-)") },
                            modifier = Modifier.testTag("filter_expense"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            // Section Header: Riwayat Catatan
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Riwayat Catatan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${displayedTransactions.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Empty state if no transactions
            if (displayedTransactions.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .testTag("empty_state_view"),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Tidak ada transaksi di bulan ini",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Gunakan tombol Tambah Transaksi di bawah untuk mencatat kas Anda.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // List of transactions
                items(
                    items = displayedTransactions,
                    key = { it.id }
                ) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        onEdit = { viewModel.openEditForm(it) },
                        onDelete = { viewModel.deleteTransaction(it) }
                    )
                }
            }
        }
    }

    // Modal Form for Add/Edit
    if (isFormOpen) {
        TransactionFormDialog(
            formState = formState,
            onDateChange = { viewModel.updateFormDate(it) },
            onTypeChange = { viewModel.updateFormType(it) },
            onAmountChange = { viewModel.updateFormAmount(it) },
            onCategoryChange = { viewModel.updateFormCategory(it) },
            onDescriptionChange = { viewModel.updateFormDescription(it) },
            onSave = { viewModel.saveTransaction() },
            onDismiss = { viewModel.closeForm() }
        )
    }
}
