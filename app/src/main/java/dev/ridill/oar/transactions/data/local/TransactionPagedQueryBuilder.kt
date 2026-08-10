package dev.ridill.oar.transactions.data.local

import androidx.room.RoomRawQuery
import dev.ridill.oar.core.data.db.FtsQueryFormatter
import dev.ridill.oar.core.data.db.KeysetColumn
import dev.ridill.oar.core.data.db.KeysetPagedQuery
import dev.ridill.oar.core.data.db.PageLoadDirection
import dev.ridill.oar.core.data.db.SortDirection
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.util.Empty

/**
 * Builds the paged transactions query with only the WHERE predicates for filters that are
 * actually active, so SQLite can use indices for whichever ones are present instead of always
 * full-scanning (the previous `(:param IS NULL OR column = :param)` form was never sargable).
 */
class TransactionPagedQueryBuilder(
    private val formatter: FtsQueryFormatter
) {

    private val columns = listOf(
        KeysetColumn("DATE(cycleStartDate)", SortDirection.DESC),
        KeysetColumn("DATE(cycleEndDate)", SortDirection.DESC),
        KeysetColumn("DATETIME(transactionTimestamp)", SortDirection.DESC),
        KeysetColumn("transactionId", SortDirection.DESC)
    )

    fun build(
        query: String?,
        cycleIds: Set<Long>?,
        movement: FundMovement?,
        showExcluded: Boolean,
        tagIds: Set<Long>?,
        folderId: Long?,
        currencyCode: String?,
        key: TransactionPageKey?,
        direction: PageLoadDirection,
        limit: Int
    ): RoomRawQuery {
        val builder = KeysetPagedQuery("transaction_details_view", columns)
        val matchQuery = formatter.prefixMatchOrNull(query)

        if (matchQuery != null) {
            builder.where(
                """transactionId IN (
                    SELECT rowid FROM transaction_fts WHERE transaction_fts MATCH ?
                    UNION
                    SELECT id FROM transaction_table
                     WHERE tag_id IN (SELECT rowid FROM tag_fts WHERE tag_fts MATCH ?)
                    UNION
                    SELECT id FROM transaction_table
                     WHERE folder_id IN (SELECT rowid FROM folder_fts WHERE folder_fts MATCH ?)
                    UNION
                    SELECT transaction_id FROM money_pile_transactions_table
                     WHERE pile_id IN (SELECT rowid FROM money_pile_fts WHERE money_pile_fts MATCH ?)
                    UNION
                    SELECT id FROM transaction_table WHERE amount LIKE ? || '%'
                )"""
            ) { s, i ->
                s.bindText(i, matchQuery); s.bindText(i + 1, matchQuery)
                s.bindText(i + 2, matchQuery); s.bindText(i + 3, matchQuery)
                s.bindText(i + 4, query ?: String.Empty)
                i + 5
            }
        }
        builder.whereLongIn("cycleId", cycleIds)
        if (movement != null) {
            builder.where("fundMovement = ?") { s, i -> s.bindText(i, movement.name); i + 1 }
        }
        builder.whereLongIn("tagId", tagIds)
        if (folderId != null) {
            builder.where("folderId = ?") { s, i -> s.bindLong(i, folderId); i + 1 }
        }
        if (!showExcluded) {
            builder.where("excluded = 0")
        }
        if (currencyCode != null) {
            builder.where("currencyCode = ?") { s, i -> s.bindText(i, currencyCode); i + 1 }
        }

        return builder.build(key, direction, limit)
    }
}
