package dev.ridill.oar.tags.data.local

import androidx.room.RoomRawQuery
import dev.ridill.oar.core.data.db.FtsQueryFormatter
import dev.ridill.oar.core.data.db.KeysetColumn
import dev.ridill.oar.core.data.db.KeysetPagedQuery
import dev.ridill.oar.core.data.db.PageLoadDirection
import dev.ridill.oar.core.data.db.SortDirection

/**
 * Builds the paged tags query, shared by the general tag list and the tag-selection search since
 * they differ only in the idIgnoreSet filter and in what an empty query means: the general list
 * treats it as "match everything", while selection search (requireNonBlankQuery) only shows
 * results once the user has actually typed something.
 */
class TagPagedQueryBuilder(
    private val formatter: FtsQueryFormatter
) {

    private val columns = listOf(
        KeysetColumn("DATETIME(created_timestamp)", SortDirection.DESC),
        KeysetColumn("name", SortDirection.ASC),
        KeysetColumn("id", SortDirection.ASC)
    )

    fun build(
        query: String,
        requireNonBlankQuery: Boolean,
        idIgnoreSet: Set<Long>?,
        cursor: TagPageKey?,
        direction: PageLoadDirection,
        limit: Int
    ): RoomRawQuery {
        val builder = KeysetPagedQuery("tag_table", columns)
        val matchQuery = formatter.prefixMatchOrNull(query)

        if (requireNonBlankQuery) {
            if (matchQuery != null) {
                builder.where("id IN (SELECT rowid FROM tag_fts WHERE tag_fts MATCH ?)") { s, i ->
                    s.bindText(i, matchQuery); i + 1
                }
            } else {
                builder.where("0")
            }
        } else if (matchQuery != null) {
            builder.where("id IN (SELECT rowid FROM tag_fts WHERE tag_fts MATCH ?)") { s, i ->
                s.bindText(i, matchQuery); i + 1
            }
        }
        builder.whereLongIn("id", idIgnoreSet, negate = true)

        return builder.build(cursor, direction, limit)
    }
}
