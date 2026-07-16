package dev.ridill.oar.core.data.db

import androidx.room.RoomRawQuery
import androidx.sqlite.SQLiteStatement

/** Binds one or more placeholders starting at `startIndex`; returns the next free 1-based index. */
typealias StatementBinder = (statement: SQLiteStatement, startIndex: Int) -> Int

/** Common shape of every feature's `*PageKey`: the ordered cursor values fed to [KeysetCursor]. */
interface KeysetPageKey {
    /** Cursor values in the same order as the query's [KeysetColumn] list. */
    fun toValues(): List<Any>
}

/**
 * Builds keyset-paginated `SELECT * FROM table WHERE ... ORDER BY ... LIMIT ?` raw queries,
 * collapsing the conditions/binders/SQL assembly repeated across the `*PagedQueryBuilder`s.
 */
class KeysetPagedQuery(
    private val table: String,
    private val columns: List<KeysetColumn>
) {
    private val conditions = mutableListOf("1 = 1")
    private val binders = mutableListOf<StatementBinder>()

    /**
     * ANDs [sql] into the WHERE clause. [bind] binds its placeholders given the next free 1-based
     * statement index, and must return the following free index; omit it for a bind-free
     * condition (e.g. a boolean-flag column check).
     */
    fun where(sql: String, bind: StatementBinder? = null) = apply {
        conditions += sql
        if (bind != null) binders += bind
    }

    /** `column IN (?, ...)` (or `NOT IN`) over Long ids; no-op when [ids] is null/empty. */
    fun whereLongIn(column: String, ids: Collection<Long>?, negate: Boolean = false) = apply {
        if (ids.isNullOrEmpty()) return@apply
        conditions += "$column ${if (negate) "NOT IN" else "IN"} (${ids.joinToString(",") { "?" }})"
        binders += { s, i ->
            var index = i
            ids.forEach { s.bindLong(index++, it) }
            index
        }
    }

    /**
     * Assembles the final raw query: registered [where]/[whereLongIn] conditions, plus (when
     * [cursor] is non-null) the [KeysetCursor] seek predicate for [direction], ordered per
     * [columns] and capped at [limit] rows.
     */
    fun build(cursor: KeysetPageKey?, direction: PageLoadDirection, limit: Int): RoomRawQuery {
        val allConditions = conditions.toMutableList()
        if (cursor != null) {
            allConditions += KeysetCursor.whereClauseSql(columns, direction)
        }

        val sql = """
            SELECT * FROM $table
            WHERE ${allConditions.joinToString(" AND ")}
            ORDER BY ${KeysetCursor.orderBySql(columns, direction)}
            LIMIT ?
        """.trimIndent()

        return RoomRawQuery(sql) { statement ->
            var index = 1
            binders.forEach { bind -> index = bind(statement, index) }
            if (cursor != null) {
                index = KeysetCursor.bindWhereClause(cursor.toValues(), statement, index)
            }
            statement.bindLong(index, limit.toLong())
        }
    }
}
