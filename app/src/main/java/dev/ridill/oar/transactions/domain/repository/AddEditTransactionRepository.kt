package dev.ridill.oar.transactions.domain.repository

import dev.ridill.oar.transactions.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface AddEditTransactionRepository {
    suspend fun getTransactionById(id: Long): Transaction?
    fun getAmountRecommendations(): Flow<List<Long>>
    suspend fun saveTransaction(transaction: Transaction): Long
    suspend fun deleteTransaction(id: Long)
    suspend fun toggleExclusionById(id: Long, excluded: Boolean)
    fun getFolderNameForId(id: Long?): Flow<String?>
}