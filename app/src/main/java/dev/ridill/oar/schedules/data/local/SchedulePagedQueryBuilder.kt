package dev.ridill.oar.schedules.data.local

import androidx.room.RoomRawQuery
import dev.ridill.oar.core.data.db.KeysetColumn
import dev.ridill.oar.core.data.db.KeysetPagedQuery
import dev.ridill.oar.core.data.db.PageLoadDirection
import dev.ridill.oar.core.data.db.SortDirection
import dev.ridill.oar.core.domain.util.UtilConstants
import java.time.LocalDate

/**
 * Mirrors the original paged list's CASE-based bucket order (this-month=0, past-due=1,
 * upcoming=2, no-next-payment=3), with next_payment_timestamp then id added as tie-breakers -
 * keyset pagination needs a deterministic total order, which the original query (bucket only)
 * didn't provide.
 *
 * [dateNow] is inlined into the bucket CASE as a literal rather than bound as a query parameter,
 * because the OR-chain keyset predicate below repeats the bucket expression across multiple
 * branches - each occurrence would otherwise need its own duplicate bind. It's safe to inline
 * since it's an app-computed month/year string, never user input.
 *
 * next_payment_timestamp is null for retired schedules (bucket 3), so it's wrapped in COALESCE:
 * SQL NULL never equals itself, which would otherwise break the keyset cursor's equality checks
 * for later columns within that bucket.
 */
object SchedulePagedQueryBuilder {

    const val NULL_TIMESTAMP_SENTINEL = "9999-99-99 99:99:99"

    fun monthYearKey(date: LocalDate): String = "%02d-%04d".format(date.monthValue, date.year)

    fun bucketFor(nextPaymentMonthKey: String?, nowMonthKey: String): Long = when {
        nextPaymentMonthKey == null -> 3L
        nextPaymentMonthKey == nowMonthKey -> 0L
        nextPaymentMonthKey < nowMonthKey -> 1L
        else -> 2L
    }

    private fun bucketExprSql(monthKey: String): String = """
        CASE
            WHEN strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', next_payment_timestamp) = '$monthKey' THEN 0
            WHEN strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', next_payment_timestamp) < '$monthKey' THEN 1
            WHEN next_payment_timestamp IS NULL THEN 3
            ELSE 2
        END
    """.trimIndent()

    private fun columns(monthKey: String): List<KeysetColumn> = listOf(
        KeysetColumn(bucketExprSql(monthKey), SortDirection.ASC),
        KeysetColumn(
            "COALESCE(DATETIME(next_payment_timestamp), '$NULL_TIMESTAMP_SENTINEL')",
            SortDirection.ASC
        ),
        KeysetColumn("id", SortDirection.ASC)
    )

    fun build(
        dateNow: LocalDate,
        cursor: SchedulePageKey?,
        direction: PageLoadDirection,
        limit: Int
    ): RoomRawQuery {
        val builder = KeysetPagedQuery("schedules_table", columns(monthYearKey(dateNow)))
        return builder.build(cursor, direction, limit)
    }
}
