package dev.ridill.oar.moneyPiles.data.local

import androidx.room.RoomRawQuery
import dev.ridill.oar.core.data.db.FtsQueryFormatter
import dev.ridill.oar.core.data.db.KeysetColumn
import dev.ridill.oar.core.data.db.KeysetPagedQuery
import dev.ridill.oar.core.data.db.PageLoadDirection
import dev.ridill.oar.core.data.db.SortDirection

class MoneyPilePagedQueryBuilder(
    private val formatter: FtsQueryFormatter
) {
    private val columns = listOf(
        KeysetColumn("(completionTimestamp IS NOT NULL)", SortDirection.ASC),
        KeysetColumn("id", SortDirection.DESC)
    )

    fun build(
        query: String,
        cursor: MoneyPilePageKey?,
        direction: PageLoadDirection,
        limit: Int,
        includeCompleted: Boolean = true,
    ): RoomRawQuery {
        val builder = KeysetPagedQuery("money_pile_aggregate_view", columns)
        val matchQuery = formatter.prefixMatchOrNull(query)

        if (matchQuery != null) {
            builder.where("id IN (SELECT rowid FROM money_pile_fts WHERE money_pile_fts MATCH ?)") { s, i ->
                s.bindText(i, matchQuery); i + 1
            }
        }
        if (!includeCompleted) {
            builder.where("completionTimestamp IS NULL")
        }

        return builder.build(cursor, direction, limit)
    }
}
