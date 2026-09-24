package dev.ridill.oar.schedules.domain.util

import dev.ridill.oar.schedules.domain.model.ScheduleRepetition
import java.time.LocalDateTime
import java.time.Month
import java.time.Year

/**
 * Pure date arithmetic for schedule recurrence. Kept dependency-free so it can be
 * unit tested directly without standing up [dev.ridill.oar.schedules.data.repository.SchedulesRepositoryImpl]'s
 * full dependency graph.
 */
class ScheduleDateCalculator {

    /**
     * Computes the timestamp of the next occurrence of a schedule, one [repetition]
     * period after the schedule's own due date.
     *
     * When [expectedTimestamp] (the schedule's previously recorded due date) is available,
     * it — not [anchor] (the moment the payment is actually being recorded) — is used as the
     * base for the shift. This keeps the schedule's cadence anchored to its original calendar
     * position instead of letting an early/late payment shift every future occurrence.
     *
     * [originalDueDate] — the due date/time as originally set by the user, unaffected by any
     * clamping from prior short-month/non-leap-year shifts — is used to restore the intended
     * day-of-month (for [ScheduleRepetition.MONTHLY]/[ScheduleRepetition.BI_MONTHLY]) or
     * month-and-day (for [ScheduleRepetition.YEARLY]) on the shifted result. Without this, a
     * schedule due on the 31st would permanently drift to the 28th/29th/30th the first time it
     * passes through a shorter month, since each occurrence would otherwise be computed by
     * shifting the *previous* (already-clamped) date rather than the original one.
     *
     * Falls back to shifting [anchor] itself when no [expectedTimestamp] is known yet
     * (e.g. a brand-new schedule).
     *
     * Returns `null` for [ScheduleRepetition.NO_REPEAT].
     */
    fun calculateNextPaymentTimestamp(
        anchor: LocalDateTime,
        repetition: ScheduleRepetition,
        expectedTimestamp: LocalDateTime? = null,
        originalDueDate: LocalDateTime? = null,
    ): LocalDateTime? {
        val base = expectedTimestamp ?: anchor
        val shifted = when (repetition) {
            ScheduleRepetition.NO_REPEAT -> return null
            ScheduleRepetition.WEEKLY -> base.plusWeeks(1)
            ScheduleRepetition.MONTHLY -> base.plusMonths(1)
            ScheduleRepetition.BI_MONTHLY -> base.plusMonths(2)
            ScheduleRepetition.YEARLY -> base.plusYears(1)
        }
        return restoreCanonicalDay(shifted, originalDueDate ?: base, repetition)
    }

    /**
     * Computes the timestamp of the previous occurrence of a schedule, one [repetition]
     * period before the schedule's own due date. Mirrors [calculateNextPaymentTimestamp],
     * shifting backwards instead.
     *
     * Returns `null` for [ScheduleRepetition.NO_REPEAT].
     */
    fun calculateLastPaymentTimestamp(
        anchor: LocalDateTime,
        repetition: ScheduleRepetition,
        expectedTimestamp: LocalDateTime? = null,
        originalDueDate: LocalDateTime? = null,
    ): LocalDateTime? {
        val base = expectedTimestamp ?: anchor
        val shifted = when (repetition) {
            ScheduleRepetition.NO_REPEAT -> return null
            ScheduleRepetition.WEEKLY -> base.minusWeeks(1)
            ScheduleRepetition.MONTHLY -> base.minusMonths(1)
            ScheduleRepetition.BI_MONTHLY -> base.minusMonths(2)
            ScheduleRepetition.YEARLY -> base.minusYears(1)
        }
        return restoreCanonicalDay(shifted, originalDueDate ?: base, repetition)
    }

    /**
     * Re-applies [canonical]'s day-of-month (or, for [ScheduleRepetition.YEARLY], its
     * month-and-day) onto [shifted], clamping to [shifted]'s own period length when the
     * canonical day doesn't exist there (e.g. the 31st in a 30-day month, or Feb 29th in a
     * non-leap year). [ScheduleRepetition.WEEKLY] periods are always 7 days long regardless of
     * calendar position, so no correction is needed there.
     */
    private fun restoreCanonicalDay(
        shifted: LocalDateTime,
        canonical: LocalDateTime,
        repetition: ScheduleRepetition
    ): LocalDateTime = when (repetition) {
        ScheduleRepetition.MONTHLY, ScheduleRepetition.BI_MONTHLY -> {
            val isLeapYear = Year.isLeap(shifted.year.toLong())
            val clampedDay = canonical.dayOfMonth.coerceAtMost(shifted.month.length(isLeapYear))
            shifted
                .withDayOfMonth(clampedDay)
                .withHour(canonical.hour)
                .withMinute(canonical.minute)
                .withSecond(canonical.second)
                .withNano(canonical.nano)
        }

        ScheduleRepetition.YEARLY -> {
            val isLeapYear = Year.isLeap(shifted.year.toLong())
            val clampedDay = if (canonical.month == Month.FEBRUARY && canonical.dayOfMonth == 29 && !isLeapYear) {
                28
            } else {
                canonical.dayOfMonth
            }
            LocalDateTime.of(
                shifted.year,
                canonical.month,
                clampedDay,
                canonical.hour,
                canonical.minute,
                canonical.second,
                canonical.nano
            )
        }

        ScheduleRepetition.WEEKLY, ScheduleRepetition.NO_REPEAT -> shifted
    }
}
