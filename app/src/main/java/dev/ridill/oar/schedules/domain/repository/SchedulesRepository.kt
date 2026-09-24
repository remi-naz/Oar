package dev.ridill.oar.schedules.domain.repository

import dev.ridill.oar.schedules.domain.model.Schedule
import dev.ridill.oar.schedules.domain.model.ScheduleRepetition
import java.time.LocalDateTime

interface SchedulesRepository {
    suspend fun getScheduleById(id: Long): Schedule?

    /**
     * Computes the timestamp of the next occurrence of a schedule, one [repetition]
     * period after the schedule's own due date.
     *
     * When [expectedTimestamp] (the schedule's previously recorded due date) is known,
     * it — not [anchor] (the moment the payment is actually being recorded) — is used as
     * the base for the shift. This keeps the schedule's cadence anchored to its original
     * calendar position instead of letting an early/late payment shift every future
     * occurrence.
     *
     * Falls back to shifting [anchor] itself when [expectedTimestamp] is `null`
     * (e.g. a brand-new schedule with no due date yet).
     *
     * [originalDueDate] — the due date/time as originally set by the user — is used to restore
     * the intended day-of-month/day-of-year on the result, so a schedule due on the 31st
     * doesn't permanently drift to an earlier day after passing through a shorter month.
     *
     * Returns `null` for [ScheduleRepetition.NO_REPEAT].
     *
     * See [dev.ridill.oar.schedules.domain.util.ScheduleDateCalculator] for the
     * implementation.
     */
    fun calculateNextPaymentTimestampFromDate(
        anchor: LocalDateTime,
        repetition: ScheduleRepetition,
        expectedTimestamp: LocalDateTime? = null,
        originalDueDate: LocalDateTime? = null,
    ): LocalDateTime?

    /**
     * Computes the timestamp of the previous occurrence of a schedule, one [repetition]
     * period before the schedule's own due date. Mirrors
     * [calculateNextPaymentTimestampFromDate], shifting backwards instead.
     *
     * Returns `null` for [ScheduleRepetition.NO_REPEAT].
     */
    fun calculateLastPaymentTimestampFromDate(
        anchor: LocalDateTime,
        repetition: ScheduleRepetition,
        expectedTimestamp: LocalDateTime? = null,
        originalDueDate: LocalDateTime? = null,
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