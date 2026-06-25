package dev.ridill.oar.transactions.domain.repository

import androidx.paging.PagingData
import dev.ridill.oar.transactions.domain.model.TransactionEntry
import dev.ridill.oar.transactions.domain.model.TransactionListItemUIModel
import dev.ridill.oar.transactions.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.util.Currency

interface AllTransactionsRepository {
    fun getAllTransactionsPaged(
        cycleIds: Set<Long>? = null,
        transactionType: TransactionType? = null,
        showExcluded: Boolean = true,
        tagIds: Set<Long>? = null,
        folderId: Long? = null,
        currency: Currency? = null
    ): Flow<PagingData<TransactionListItemUIModel>>

    fun getSearchResults(
        query: String?
    ): Flow<PagingData<TransactionEntry>>

    suspend fun deleteTransactionsByIds(ids: Set<Long>)
    suspend fun setTagIdToTransactions(tagId: Long?, transactionIds: Set<Long>)
    fun getShowExcludedOption(): Flow<Boolean>
    suspend fun toggleShowExcludedOption(show: Boolean)
    suspend fun toggleTransactionExclusionByIds(ids: Set<Long>, excluded: Boolean)
    suspend fun addTransactionsToFolderByIds(ids: Set<Long>, folderId: Long)
    suspend fun removeTransactionsFromFolders(ids: Set<Long>)
    suspend fun aggregateTogether(ids: Set<Long>, dateTime: LocalDateTime): Long
    suspend fun updateCycleForTransactions(ids: Set<Long>, cycleId: Long)
    suspend fun getTransactionIdsForCycle(cycleId: Long): List<Long>
}