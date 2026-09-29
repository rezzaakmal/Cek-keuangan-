package com.example.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {
    fun getAllTransactions(): Flow<List<TransactionEntity>> = dao.getAllTransactions()

    fun getTransactionsByMonth(monthPrefix: String): Flow<List<TransactionEntity>> =
        dao.getTransactionsByMonth(monthPrefix)

    suspend fun getTransactionById(id: Long): TransactionEntity? = dao.getTransactionById(id)

    suspend fun insert(transaction: TransactionEntity): Long = dao.insert(transaction)

    suspend fun update(transaction: TransactionEntity) = dao.update(transaction)

    suspend fun delete(transaction: TransactionEntity) = dao.delete(transaction)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun insertAll(list: List<TransactionEntity>) = dao.insertAll(list)
}
