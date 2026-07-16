package dev.ridill.oar.core.data.db

import androidx.sqlite.SQLiteStatement

enum class SortDirection { ASC, DESC }

enum class PageLoadDirection {
    /** Fetch rows continuing in the query's display order (Refresh/Append). */
    FORWARD,

    /** Fetch rows preceding the cursor (Prepend); caller reverses the fetched rows. */
    BACKWARD
}

/** One column of a keyset ORDER BY, expressed in the query's forward (display) direction. */
data class KeysetColumn(
    val expr: String,
    val direction: SortDirection
)

/**
 * Builds ORDER BY / WHERE clauses for keyset ("seek method") pagination over columns that may
 * sort in different directions per column (e.g. name ASC, created_timestamp DESC), where a
 * single tuple inequality comparison doesn't work. Produces the standard seek-method OR-chain:
 * (c0 OP ?) OR (c0 = ? AND c1 OP ?) OR (c0 = ? AND c1 = ? AND c2 OP ?) ...
 *
 * The last column must be a total tie-breaker (e.g. the primary key) so every row has a
 * deterministic position. Any nullable column's [KeysetColumn.expr] must be wrapped (e.g.
 * COALESCE) to never evaluate to SQL NULL - NULL never equals itself, which would silently break
 * the equality checks later columns in the chain depend on.
 */
object KeysetCursor {

    fun orderBySql(columns: List<KeysetColumn>, loadDirection: PageLoadDirection): String =
        columns.joinToString(", ") { "${it.expr} ${effectiveDirection(it.direction, loadDirection)}" }

    fun whereClauseSql(columns: List<KeysetColumn>, loadDirection: PageLoadDirection): String =
        columns.indices.joinToString(" OR ", prefix = "(", postfix = ")") { i ->
            val equalities = (0 until i).joinToString(" AND ") { j -> "${columns[j].expr} = ?" }
            val inequality = "${columns[i].expr} ${comparisonOperator(columns[i].direction, loadDirection)} ?"
            if (equalities.isEmpty()) inequality else "$equalities AND $inequality"
        }

    /**
     * Binds [cursorValues] (one per column, each a Long or String, in column order) to match
     * [whereClauseSql]'s placeholders - earlier columns are re-bound once per subsequent OR
     * branch. Returns the next unused 1-based statement index.
     */
    fun bindWhereClause(
        cursorValues: List<Any>,
        statement: SQLiteStatement,
        startIndex: Int
    ): Int {
        var index = startIndex
        for (i in cursorValues.indices) {
            for (j in 0 until i) {
                bindValue(statement, index, cursorValues[j])
                index++
            }
            bindValue(statement, index, cursorValues[i])
            index++
        }
        return index
    }

    private fun bindValue(statement: SQLiteStatement, index: Int, value: Any) {
        when (value) {
            is Long -> statement.bindLong(index, value)
            is String -> statement.bindText(index, value)
            else -> error("Unsupported keyset cursor value type: ${value::class}")
        }
    }

    private fun effectiveDirection(direction: SortDirection, loadDirection: PageLoadDirection): SortDirection =
        if (loadDirection == PageLoadDirection.FORWARD) direction
        else if (direction == SortDirection.ASC) SortDirection.DESC else SortDirection.ASC

    private fun comparisonOperator(direction: SortDirection, loadDirection: PageLoadDirection): String =
        if (effectiveDirection(direction, loadDirection) == SortDirection.ASC) ">" else "<"
}
