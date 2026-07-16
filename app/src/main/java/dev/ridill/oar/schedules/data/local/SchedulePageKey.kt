package dev.ridill.oar.schedules.data.local

import dev.ridill.oar.core.data.db.KeysetPageKey
import dev.ridill.oar.core.data.db.toSqliteUtcDateTimeString
import dev.ridill.oar.schedules.data.local.entity.ScheduleEntity
import java.time.LocalDate

/**
 * Keyset cursor mirroring SchedulePagedQueryBuilder's ORDER BY (bucket ASC,
 * next_payment_timestamp ASC, id ASC).
 */
data class SchedulePageKey(
    val bucket: Long,
    val nextPaymentTimestamp: String,
    val id: Long
) : KeysetPageKey {
    override fun toValues(): List<Any> = listOf(bucket, nextPaymentTimestamp, id)
}

fun ScheduleEntity.toPageKey(dateNow: LocalDate): SchedulePageKey {
    val nowMonthKey = SchedulePagedQueryBuilder.monthYearKey(dateNow)
    val nextMonthKey = nextPaymentTimestamp?.let {
        SchedulePagedQueryBuilder.monthYearKey(it.toLocalDate())
    }
    return SchedulePageKey(
        bucket = SchedulePagedQueryBuilder.bucketFor(nextMonthKey, nowMonthKey),
        nextPaymentTimestamp = nextPaymentTimestamp?.toSqliteUtcDateTimeString()
            ?: SchedulePagedQueryBuilder.NULL_TIMESTAMP_SENTINEL,
        id = id
    )
}
