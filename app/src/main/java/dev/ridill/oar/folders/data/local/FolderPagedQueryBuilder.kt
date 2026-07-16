package dev.ridill.oar.folders.data.local

import androidx.room.RoomRawQuery
import dev.ridill.oar.core.data.db.KeysetColumn
import dev.ridill.oar.core.data.db.KeysetPagedQuery
import dev.ridill.oar.core.data.db.PageLoadDirection
import dev.ridill.oar.core.data.db.SortDirection

object FolderPagedQueryBuilder {

    private val COLUMNS = listOf(
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
        val builder = KeysetPagedQuery("folder_table", COLUMNS)

        if (query.isNotBlank()) {
            builder.where("name LIKE '%' || ? || '%'") { s, i -> s.bindText(i, query); i + 1 }
        }

        return builder.build(cursor, direction, limit)
    }
}
