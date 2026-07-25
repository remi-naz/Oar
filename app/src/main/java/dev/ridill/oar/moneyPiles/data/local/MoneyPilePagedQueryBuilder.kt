package dev.ridill.oar.moneyPiles.data.local

import androidx.room.RoomRawQuery
import dev.ridill.oar.core.data.db.KeysetColumn
import dev.ridill.oar.core.data.db.KeysetPagedQuery
import dev.ridill.oar.core.data.db.PageLoadDirection
import dev.ridill.oar.core.data.db.SortDirection

object MoneyPilePagedQueryBuilder {
    private val COLUMNS = listOf(
        KeysetColumn("id", SortDirection.DESC)
    )

    fun build(
        query: String,
        cursor: MoneyPilePageKey?,
        direction: PageLoadDirection,
        limit: Int,
    ): RoomRawQuery {
        val builder = KeysetPagedQuery("money_pile_aggregate_view", COLUMNS)

        if (query.isNotBlank()) {
            builder.where("name LIKE '%' || ? || '%'") { s, i -> s.bindText(i, query); i + 1 }
        }

        return builder.build(cursor, direction, limit)
    }
}