package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    onEdit: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val isIncome = transaction.type == "in"
    val badgeColor = if (isIncome) IncomeGreenContainer else ExpenseRedContainer
    val iconColor = if (isIncome) IncomeGreen else ExpenseRed
    val amountPrefix = if (isIncome) "+" else "-"
    val amountColor = if (isIncome) IncomeGreen else ExpenseRed

    val categoryIcon = getCategoryIcon(transaction.category, isIncome)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = transaction.category,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Category and description
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = transaction.category,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "• ${DateUtils.formatDisplayDate(transaction.date)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (transaction.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = transaction.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Amount
                Text(
                    text = "$amountPrefix ${CurrencyFormatter.formatRupiah(transaction.amount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            }

            // Action row: Edit & Hapus buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onEdit(transaction) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("edit_transaction_${transaction.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Transaksi",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_transaction_${transaction.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Transaksi",
                        tint = ExpenseRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(text = "Hapus Catatan?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = "Apakah Anda yakin ingin menghapus transaksi '${transaction.category}' sebesar ${CurrencyFormatter.formatRupiah(transaction.amount)}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(transaction)
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text(text = "Hapus", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(text = "Batal")
                }
            }
        )
    }
}

private fun getCategoryIcon(category: String, isIncome: Boolean): ImageVector {
    val cat = category.lowercase()
    return when {
        cat.contains("makan") || cat.contains("minum") || cat.contains("restoran") || cat.contains("kopi") -> Icons.Default.Restaurant
        cat.contains("gaji") || cat.contains("bonus") || cat.contains("salary") -> Icons.Default.Payments
        cat.contains("belanja") || cat.contains("market") || cat.contains("mall") -> Icons.Default.ShoppingBag
        cat.contains("transport") || cat.contains("bensin") || cat.contains("ojol") || cat.contains("parkir") -> Icons.Default.DirectionsCar
        cat.contains("tagihan") || cat.contains("listrik") || cat.contains("air") || cat.contains("wifi") || cat.contains("pulsa") -> Icons.Default.ReceiptLong
        cat.contains("sehat") || cat.contains("obat") || cat.contains("dokter") || cat.contains("apotek") -> Icons.Default.LocalHospital
        cat.contains("hiburan") || cat.contains("game") || cat.contains("nonton") || cat.contains("bioskop") -> Icons.Default.SportsEsports
        cat.contains("investasi") || cat.contains("saham") || cat.contains("crypto") || cat.contains("reksadana") -> Icons.Default.TrendingUp
        isIncome -> Icons.Default.Payments
        else -> Icons.Default.Work
    }
}
