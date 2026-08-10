package dev.ridill.oar.folders.data.local

import androidx.room.RoomRawQuery
import dev.ridill.oar.core.data.db.FtsQueryFormatter
import dev.ridill.oar.core.data.db.KeysetColumn
import dev.ridill.oar.core.data.db.KeysetPagedQuery
import dev.ridill.oar.core.data.db.PageLoadDirection
import dev.ridill.oar.core.data.db.SortDirection

class FolderPagedQueryBuilder(
    private val formatter: FtsQueryFormatter
) {

    private val columns = listOf(
        KeysetColumn("name", SortDirection.ASC),
        KeysetColumn("DATETIME(created_timestamp)", SortDirection.DESC),
        KeysetColumn("id", SortDirection.ASC)
    )

    fun build(
        query: String,
        cursor: FolderPageKey?,
        direction: PageLoadDirection,
        limit: Int
    ): RoomRawQuery {
        val builder = KeysetPagedQuery("folder_table", columns)
        val matchQuery = formatter.prefixMatchOrNull(query)

        if (matchQuery != null) {
            builder.where("id IN (SELECT rowid FROM folder_fts WHERE folder_fts MATCH ?)") { s, i ->
                s.bindText(i, matchQuery); i + 1
            }
        }

        return builder.build(cursor, direction, limit)
    }
}
