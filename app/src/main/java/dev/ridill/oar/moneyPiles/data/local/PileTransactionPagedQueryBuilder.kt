package dev.ridill.oar.moneyPiles.data.local

import androidx.room.RoomRawQuery
import dev.ridill.oar.core.data.db.KeysetColumn
import dev.ridill.oar.core.data.db.KeysetPagedQuery
import dev.ridill.oar.core.data.db.PageLoadDirection
import dev.ridill.oar.core.data.db.SortDirection

object PileTransactionPagedQueryBuilder {

    private val COLUMNS = listOf(
        KeysetColumn("pile_id", SortDirection.DESC)
    )

    fun build(
        pileId: Long,
        cursor: PileTransactionKey?,
        direction: PageLoadDirection,
        limit: Int,
    ): RoomRawQuery {
        val builder = KeysetPagedQuery("money_pile_transactions_table", COLUMNS)
        builder.where("pile_id = ?") { s, i -> s.bindLong(i, pileId); i + 1 }

        return builder.build(cursor, direction, limit)
    }
}