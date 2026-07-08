package dev.ridill.oar.folders.domain.repository

import androidx.paging.PagingData
import dev.ridill.oar.folders.domain.model.FolderDetails
import dev.ridill.oar.transactions.domain.model.TransactionListItemUIModel
import kotlinx.coroutines.flow.Flow

interface FolderDetailsRepository {
    fun getFolderDetailsById(id: Long): Flow<FolderDetails?>
    suspend fun deleteFolderById(id: Long)
    suspend fun deleteFolderWithTransactions(id: Long)
    fun getTransactionsInFolderPaged(
        folderId: Long
    ): Flow<PagingData<TransactionListItemUIModel>>

    suspend fun addTransactionsToFolderByIds(folderId: Long, transactionIds: Set<Long>)
    suspend fun deleteTransactionsByIds(ids: Set<Long>)
    suspend fun removeTransactionFromFolderById(ids: Set<Long>)
    suspend fun addTransactionToFolder(txId: Long, folderId: Long)
    fun shouldShowActionPreview(): Flow<Boolean>
    suspend fun disableActionPreview()
    suspend fun getTransactionIdsInFolder(
        cycleId: Long,
        folderId: Long
    ): List<Long>
}