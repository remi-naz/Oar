package dev.ridill.oar.transactions.data.local

import dev.ridill.oar.core.data.db.KeysetPagingSource
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.data.db.PageLoadDirection
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.transactions.data.local.views.TransactionDetailsView
import kotlinx.coroutines.CoroutineScope

/**
 * Keyset-paginated alternative to Room's generated OFFSET-based PagingSource. Each page's query
 * starts from the last-seen row's [TransactionPageKey] instead of a numeric offset, so cost
 * shrinks with the remaining unseen rows instead of staying constant on every page load.
 */
class TransactionPagingSource(
    private val dao: TransactionDao,
    db: OarDatabase,
    applicationScope: CoroutineScope,
    private val query: String?,
    private val cycleIds: Set<Long>?,
    private val type: FundMovement?,
    private val showExcluded: Boolean,
    private val tagIds: Set<Long>?,
    private val folderId: Long?,
    private val currencyCode: String?
) : KeysetPagingSource<TransactionPageKey, TransactionDetailsView>(
    db = db,
    applicationScope = applicationScope,
    invalidationTables = setOf(
        "transaction_table",
        "budget_cycle_table",
        "tag_table",
        "folder_table"
    )
) {

    override suspend fun fetch(
        cursor: TransactionPageKey?,
        direction: PageLoadDirection,
        loadSize: Int
    ): List<TransactionDetailsView> {
        val rawQuery = TransactionPagedQueryBuilder.build(
            query = query,
            cycleIds = cycleIds,
            movement = type,
            showExcluded = showExcluded,
            tagIds = tagIds,
            folderId = folderId,
            currencyCode = currencyCode,
            key = cursor,
            direction = direction,
            limit = loadSize
        )
        return dao.getTransactionsPagedRaw(rawQuery)
    }

    override fun keyOf(value: TransactionDetailsView): TransactionPageKey = value.toPageKey()
}
