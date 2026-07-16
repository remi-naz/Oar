package dev.ridill.oar.transactions.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.di.ApplicationScope
import dev.ridill.oar.transactions.data.local.TransactionDao
import dev.ridill.oar.transactions.data.local.TransactionPagingSource
import dev.ridill.oar.transactions.data.local.entity.TransactionEntity
import dev.ridill.oar.transactions.data.local.views.TransactionDetailsView
import dev.ridill.oar.transactions.data.toTransaction
import dev.ridill.oar.transactions.data.toTransactionListItem
import dev.ridill.oar.transactions.domain.model.Transaction
import dev.ridill.oar.transactions.domain.model.TransactionEntry
import dev.ridill.oar.transactions.domain.model.TransactionListItemUIModel
import dev.ridill.oar.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.Currency

class TransactionRepositoryImpl(
    private val dao: TransactionDao,
    private val db: OarDatabase,
    @ApplicationScope private val applicationScope: CoroutineScope
) : TransactionRepository {
    override fun getAllTransactionsPaged(
        query: String?,
        cycleIds: Set<Long>?,
        type: FundMovement?,
        showExcluded: Boolean,
        tagIds: Set<Long>?,
        folderId: Long?,
        currency: Currency?
    ): Flow<PagingData<TransactionEntry>> = Pager(
        config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE),
        pagingSourceFactory = {
            TransactionPagingSource(
                dao = dao,
                db = db,
                applicationScope = applicationScope,
                query = query,
                cycleIds = cycleIds?.takeIf { it.isNotEmpty() },
                type = type,
                showExcluded = showExcluded,
                tagIds = tagIds?.takeIf { it.isNotEmpty() },
                folderId = folderId,
                currencyCode = currency?.currencyCode
            )
        }
    ).flow
        .mapLatest { it.map(TransactionDetailsView::toTransactionListItem) }

    override fun getDateSeparatedTransactions(
        query: String?,
        cycleIds: Set<Long>?,
        type: FundMovement?,
        showExcluded: Boolean,
        tagIds: Set<Long>?,
        folderId: Long?,
        currency: Currency?
    ): Flow<PagingData<TransactionListItemUIModel>> = getAllTransactionsPaged(
        query = query,
        cycleIds = cycleIds,
        type = type,
        showExcluded = showExcluded,
        tagIds = tagIds,
        folderId = folderId,
        currency = currency
    ).mapLatest { pagingData ->
        pagingData.map { TransactionListItemUIModel.TransactionItem(it) }
    }.mapLatest { pagingData ->
        pagingData
            .insertSeparators<TransactionListItemUIModel.TransactionItem, TransactionListItemUIModel>
            { before, after ->
                if (before?.cycleEntry?.id != after?.cycleEntry?.id) after?.cycleEntry
                    ?.let { TransactionListItemUIModel.CycleSeparator(it) }
                else null
            }
    }

    override suspend fun saveTransaction(
        cycleId: Long,
        amount: Double,
        id: Long,
        note: String?,
        timestamp: LocalDateTime,
        type: FundMovement,
        tagId: Long?,
        folderId: Long?,
        scheduleId: Long?,
        excluded: Boolean,
        currency: Currency?
    ): Transaction = withContext(Dispatchers.IO) {
        val currencyPref = currency ?: LocaleUtil.defaultCurrency
        val entity = TransactionEntity(
            id = id,
            note = note.orEmpty(),
            amount = amount,
            timestamp = timestamp,
            type = type,
            isExcluded = excluded,
            tagId = tagId,
            folderId = folderId,
            scheduleId = scheduleId,
            currencyCode = currencyPref.currencyCode,
            cycleId = cycleId
        )
        val insertedId = dao.upsert(entity).first()
        entity.copy(id = insertedId)
            .toTransaction()
    }

    override suspend fun deleteById(
        vararg ids: Long
    ) = withContext(Dispatchers.IO) {
        dao.deleteMultipleTransactionsById(ids.toSet())
    }

    override suspend fun toggleExcluded(id: Long, excluded: Boolean) = withContext(Dispatchers.IO) {
        dao.toggleExclusionByIds(setOf(id), excluded)
    }
}