package dev.ridill.oar.schedules.domain.repository

import dev.ridill.oar.schedules.domain.model.Schedule
import dev.ridill.oar.schedules.domain.model.ScheduleRepetition
import java.time.LocalDateTime

interface SchedulesRepository {
    suspend fun getScheduleById(id: Long): Schedule?

    /**
     * Computes the timestamp of the next occurrence of a schedule, one [repetition]
     * period after [anchor].
     *
     * This is *not* a plain "add one period" calculation. It happens in two steps:
     *
     * 1. **Naive shift** — [anchor] is moved forward by one [repetition] period
     *    (+1 week / +1 month / +2 months / +1 year) to get a `nextIntervalDateTime`.
     *    If [expectedTimestamp] is `null`, this naive value is returned as-is.
     *
     * 2. **Drift correction** — if [expectedTimestamp] is supplied, `nextIntervalDateTime`
     *    is further nudged by a `difference`, computed as the shifted date's own calendar
     *    position modulo the length of the period it falls in:
     *     - [ScheduleRepetition.WEEKLY]: day-of-week, modulo 7
     *     - [ScheduleRepetition.MONTHLY]: day-of-month, modulo the length of [anchor]'s month
     *     - [ScheduleRepetition.BI_MONTHLY]: day-of-month, modulo twice the length of
     *       [anchor]'s month
     *     - [ScheduleRepetition.YEARLY]: day-of-year, modulo the length of the shifted
     *       date's year
     *
     *    That `difference` is then applied on top of the naive shift:
     *     - if [anchor] is *after* [expectedTimestamp] (the last payment was logged later
     *       than originally expected), `difference` days are subtracted, pulling the next
     *       date back.
     *     - otherwise (the last payment landed on or before the expected date),
     *       `difference` days are added, pushing the next date forward.
     *
     *    This keeps the schedule's cadence anchored to its original calendar position
     *    instead of letting a single early/late payment permanently shift every future
     *    occurrence by a fixed period.
     *
     * Returns `null` for [ScheduleRepetition.NO_REPEAT].
     */
    fun calculateNextPaymentTimestampFromDate(
        anchor: LocalDateTime,
        repetition: ScheduleRepetition,
        expectedTimestamp: LocalDateTime? = null,
    ): LocalDateTime?

    /**
     * Computes the timestamp of the previous occurrence of a schedule, one [repetition]
     * period before [anchor].
     *
     * Mirrors [calculateNextPaymentTimestampFromDate], but shifting backwards:
     *
     * 1. **Naive shift** — [anchor] is moved back by one [repetition] period
     *    (-1 week / -1 month / -2 months / -1 year) to get a `prevIntervalDateTime`.
     *    If [expectedTimestamp] is `null`, this naive value is returned as-is.
     *
     * 2. **Drift correction** — if [expectedTimestamp] is supplied, `prevIntervalDateTime`
     *    is further nudged by a `difference`, computed the same way as in
     *    [calculateNextPaymentTimestampFromDate] (day-of-week/month/year of the shifted
     *    date, modulo that period's length):
     *     - if [anchor] is *after* [expectedTimestamp], `difference` days are subtracted
     *       from the shifted date.
     *     - otherwise, `difference` days are added.
     *
     * Returns `null` for [ScheduleRepetition.NO_REPEAT].
     */
    fun calculateLastPaymentTimestampFromDate(
        anchor: LocalDateTime,
        repetition: ScheduleRepetition,
        expectedTimestamp: LocalDateTime? = null,
    ): LocalDateTime?

    suspend fun saveSchedule(
        schedule: Schedule,
        setReminder: Boolean = false,
    )
    suspend fun addPaymentToSchedule(schedule: Schedule)
    suspend fun getOldestTxTimestampForSchedule(id: Long): LocalDateTime?
    suspend fun getLatestTxTimestampForSchedule(id: Long): LocalDateTime?
    suspend fun deleteScheduleById(id: Long)
    suspend fun setAllFutureScheduleReminders()
    suspend fun deleteSchedulesByIds(ids: Set<Long>)
    suspend fun updateSchedules(vararg schedule: Schedule)
}