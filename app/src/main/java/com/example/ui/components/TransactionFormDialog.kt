package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FormState
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryBlue
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormDialog(
    formState: FormState,
    onDateChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val expenseCategories = listOf("Makan & Minum", "Belanja", "Transportasi", "Tagihan", "Hiburan", "Kesehatan", "Pendidikan", "Keluarga")
    val incomeCategories = listOf("Gaji", "Bonus", "Usaha", "Investasi", "Hadiah", "Penjualan", "Freelance")

    val activeCategories = if (formState.type == "in") incomeCategories else expenseCategories

    // DatePicker Dialog setup
    val calendar = remember { Calendar.getInstance() }
    val datePickerDialog = remember(context, formState.date) {
        val parts = formState.date.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
        val month = (parts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
        val day = parts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                onDateChange(formattedDate)
            },
            year,
            month,
            day
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("transaction_form_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (formState.isEditing) "Edit Transaksi" else "Tambah Transaksi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (formState.isEditing) "ID: #${formState.id}" else "Baru",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction Type Selector (Segmented Button)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("type_segmented_button")
            ) {
                val isOut = formState.type == "out"
                val isIn = formState.type == "in"

                SegmentedButton(
                    selected = isOut,
                    onClick = { onTypeChange("out") },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = ExpenseRed.copy(alpha = 0.15f),
                        activeContentColor = ExpenseRed
                    )
                ) {
                    Text("Pengeluaran (-)", fontWeight = FontWeight.Bold)
                }

                SegmentedButton(
                    selected = isIn,
                    onClick = { onTypeChange("in") },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = IncomeGreen.copy(alpha = 0.15f),
                        activeContentColor = IncomeGreen
                    )
                ) {
                    Text("Pemasukan (+)", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Picker Clickable Field
            OutlinedTextField(
                value = DateUtils.formatDisplayDate(formState.date),
                onValueChange = {},
                readOnly = true,
                label = { Text("Tanggal Transaksi") },
                leadingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Tanggal")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() }
                    .testTag("date_picker_field"),
                enabled = false, // disabled for manual keyboard input, clicks trigger dialog
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Nominal / Amount input
            val rawAmountLong = formState.amount.toLongOrNull() ?: 0L
            val formattedHelper = if (rawAmountLong > 0) CurrencyFormatter.formatRupiah(rawAmountLong) else "Rp 0"

            OutlinedTextField(
                value = formState.amount,
                onValueChange = onAmountChange,
                label = { Text("Nominal (Rp)") },
                placeholder = { Text("0") },
                supportingText = {
                    Text(
                        text = "Format: $formattedHelper",
                        fontWeight = FontWeight.SemiBold,
                        color = if (formState.type == "in") IncomeGreen else ExpenseRed
                    )
                },
                isError = formState.errorMessage?.contains("Nominal") == true,
                leadingIcon = {
                    Icon(Icons.Default.Payments, contentDescription = "Nominal")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Quick Amount Increment Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val increments = listOf(10000L, 50000L, 100000L, 500000L, 1000000L)
                increments.forEach { inc ->
                    SuggestionChip(
                        onClick = {
                            val newTotal = rawAmountLong + inc
                            onAmountChange(newTotal.toString())
                        },
                        label = {
                            Text(
                                text = "+${CurrencyFormatter.formatNumber(inc)}",
                                fontSize = 11.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category input & Quick Chips
            OutlinedTextField(
                value = formState.category,
                onValueChange = onCategoryChange,
                label = { Text("Kategori") },
                placeholder = { Text("Makan, Gaji, Belanja, dll.") },
                isError = formState.errorMessage?.contains("Kategori") == true,
                leadingIcon = {
                    Icon(Icons.Default.Category, contentDescription = "Kategori")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("category_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Quick Category Chips
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                activeCategories.forEach { cat ->
                    val isSelected = formState.category.equals(cat, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategoryChange(cat) },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Description / Keperluan Notes
            OutlinedTextField(
                value = formState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Keterangan Keperluan (Opsional)") },
                placeholder = { Text("Contoh: Makan siang nasi padang") },
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = "Keterangan")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("description_input"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            // Error Message Banner if any
            if (formState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons (Simpan & Batal)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cancel_transaction_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (formState.isEditing) "Batal Edit" else "Batal")
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .weight(2f)
                        .testTag("save_transaction_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (formState.isEditing) "Perbarui Data" else "Simpan Transaksi",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
