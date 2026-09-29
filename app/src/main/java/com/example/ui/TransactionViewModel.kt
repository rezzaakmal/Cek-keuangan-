package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.TransactionEntity
import com.example.data.TransactionRepository
import com.example.util.CsvExporter
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TypeFilter {
    ALL, INCOME, EXPENSE
}

data class FormState(
    val id: Long = 0,
    val date: String = DateUtils.getCurrentDate(),
    val type: String = "out", // Default to pengeluaran like everyday spending
    val amount: String = "",
    val category: String = "",
    val description: String = "",
    val isEditing: Boolean = false,
    val errorMessage: String? = null
)

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(DateUtils.getCurrentMonth())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _typeFilter = MutableStateFlow(TypeFilter.ALL)
    val typeFilter: StateFlow<TypeFilter> = _typeFilter.asStateFlow()

    private val _isFormOpen = MutableStateFlow(false)
    val isFormOpen: StateFlow<Boolean> = _isFormOpen.asStateFlow()

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    // Raw transactions from Room database
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered by selected month
    val monthTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        _selectedMonth
    ) { list, month ->
        if (month.isEmpty()) {
            list
        } else {
            list.filter { it.date.startsWith(month) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered by selected month + search query + type filter
    val displayedTransactions: StateFlow<List<TransactionEntity>> = combine(
        monthTransactions,
        _searchQuery,
        _typeFilter
    ) { list, query, filter ->
        var result = list
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter {
                it.category.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.date.contains(q)
            }
        }
        when (filter) {
            TypeFilter.ALL -> result
            TypeFilter.INCOME -> result.filter { it.type == "in" }
            TypeFilter.EXPENSE -> result.filter { it.type == "out" }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Summary calculations for the selected month
    val totalIncome: StateFlow<Long> = monthTransactions.combine(_selectedMonth) { list, _ ->
        list.filter { it.type == "in" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalExpense: StateFlow<Long> = monthTransactions.combine(_selectedMonth) { list, _ ->
        list.filter { it.type == "out" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalBalance: StateFlow<Long> = combine(totalIncome, totalExpense) { inc, exp ->
        inc - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    init {
        // Seed default transactions if completely fresh
        viewModelScope.launch {
            allTransactions.collect { list ->
                if (list.isEmpty()) {
                    seedInitialData()
                }
            }
        }
    }

    private suspend fun seedInitialData() {
        val today = DateUtils.getCurrentDate()
        val samples = listOf(
            TransactionEntity(
                date = today,
                type = "in",
                amount = 7500000L,
                category = "Gaji",
                description = "Gaji Pokok Bulanan"
            ),
            TransactionEntity(
                date = today,
                type = "out",
                amount = 45000L,
                category = "Makan & Minum",
                description = "Makan Siang & Es Teh"
            ),
            TransactionEntity(
                date = today,
                type = "out",
                amount = 150000L,
                category = "Transportasi",
                description = "Bensin & Parkir"
            ),
            TransactionEntity(
                date = today,
                type = "out",
                amount = 350000L,
                category = "Belanja",
                description = "Kebutuhan Dapur & Sayuran"
            )
        )
        repository.insertAll(samples)
    }

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
    }

    fun setPreviousMonth() {
        _selectedMonth.value = DateUtils.adjustMonth(_selectedMonth.value, -1)
    }

    fun setNextMonth() {
        _selectedMonth.value = DateUtils.adjustMonth(_selectedMonth.value, 1)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(filter: TypeFilter) {
        _typeFilter.value = filter
    }

    fun openAddForm() {
        _formState.value = FormState(
            id = 0,
            date = DateUtils.getCurrentDate(),
            type = "out",
            amount = "",
            category = "",
            description = "",
            isEditing = false,
            errorMessage = null
        )
        _isFormOpen.value = true
    }

    fun openEditForm(tx: TransactionEntity) {
        _formState.value = FormState(
            id = tx.id,
            date = tx.date,
            type = tx.type,
            amount = tx.amount.toString(),
            category = tx.category,
            description = tx.description,
            isEditing = true,
            errorMessage = null
        )
        _isFormOpen.value = true
    }

    fun closeForm() {
        _isFormOpen.value = false
    }

    fun updateFormDate(date: String) {
        _formState.value = _formState.value.copy(date = date)
    }

    fun updateFormType(type: String) {
        _formState.value = _formState.value.copy(type = type)
    }

    fun updateFormAmount(amount: String) {
        // Strip non-digits
        val digits = amount.filter { it.isDigit() }
        _formState.value = _formState.value.copy(amount = digits, errorMessage = null)
    }

    fun updateFormCategory(category: String) {
        _formState.value = _formState.value.copy(category = category, errorMessage = null)
    }

    fun updateFormDescription(desc: String) {
        _formState.value = _formState.value.copy(description = desc)
    }

    fun saveTransaction() {
        val state = _formState.value
        val amountVal = state.amount.toLongOrNull() ?: 0L

        if (amountVal <= 0) {
            _formState.value = state.copy(errorMessage = "Nominal harus lebih dari 0")
            return
        }

        if (state.category.isBlank()) {
            _formState.value = state.copy(errorMessage = "Kategori tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            val entity = TransactionEntity(
                id = state.id,
                date = state.date,
                type = state.type,
                amount = amountVal,
                category = state.category.trim(),
                description = state.description.trim()
            )

            if (state.isEditing) {
                repository.update(entity)
                _snackbarMessage.emit("Transaksi berhasil diperbarui")
            } else {
                repository.insert(entity)
                _snackbarMessage.emit("Transaksi berhasil disimpan")
            }

            // Sync month filter to match the newly added/edited date
            if (state.date.length >= 7) {
                _selectedMonth.value = state.date.substring(0, 7)
            }

            _isFormOpen.value = false
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.delete(tx)
            _snackbarMessage.emit("Transaksi dihapus")
        }
    }

    fun exportToExcel(context: Context) {
        val list = monthTransactions.value
        if (list.isEmpty()) {
            viewModelScope.launch {
                _snackbarMessage.emit("Tidak ada transaksi pada bulan yang dipilih untuk diekspor.")
            }
            return
        }

        val displayMonth = DateUtils.formatMonthYear(_selectedMonth.value)
        val result = CsvExporter.exportAndShare(context, list, displayMonth)
        viewModelScope.launch {
            if (result.isSuccess) {
                _snackbarMessage.emit("Laporan berhasil disiapkan untuk dibuka di Excel")
            } else {
                _snackbarMessage.emit("Gagal mengekspor data: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}

class TransactionViewModelFactory(
    private val repository: TransactionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            return TransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
