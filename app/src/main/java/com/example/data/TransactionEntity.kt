package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val type: String, // "in" for Pemasukan, "out" for Pengeluaran
    val amount: Long,
    val category: String,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
