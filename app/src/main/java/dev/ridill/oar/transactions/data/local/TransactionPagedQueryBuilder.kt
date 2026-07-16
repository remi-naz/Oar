package dev.ridill.oar.transactions.data.local

import androidx.room.RoomRawQuery
import dev.ridill.oar.core.data.db.KeysetColumn
import dev.ridill.oar.core.data.db.KeysetPagedQuery
import dev.ridill.oar.core.data.db.PageLoadDirection
import dev.ridill.oar.core.data.db.SortDirection
import dev.ridill.oar.core.domain.model.FundMovement

/**
 * Builds the paged transactions query with only the WHERE predicates for filters that are
 * actually active, so SQLite can use indices for whichever ones are present instead of always
 * full-scanning (the previous `(:param IS NULL OR column = :param)` form was never sargable).
 */
object TransactionPagedQueryBuilder {

    private val COLUMNS = listOf(
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
        val builder = KeysetPagedQuery("transaction_details_view", COLUMNS)

        if (!query.isNullOrBlank()) {
            builder.where(
                "(transactionAmount LIKE ? || '%' OR transactionNote LIKE '%' || ? || '%' " +
                        "OR tagName LIKE '%' || ? || '%' OR folderName LIKE '%' || ? || '%')"
            ) { s, i ->
                s.bindText(i, query); s.bindText(i + 1, query)
                s.bindText(i + 2, query); s.bindText(i + 3, query)
                i + 4
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
