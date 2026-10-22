package dev.ridill.oar.budgetCycles.domain.util

import dev.ridill.oar.budgetCycles.domain.model.CycleStartDay
import java.time.LocalDate
import java.time.YearMonth

/**
 * Pure date arithmetic for budget cycle periods. Dependency-free (no clock access) so it can be
 * unit tested directly; callers pass in "today" where it matters.
 *
 * A cycle is identified by the [YearMonth] in which it *ends*. For example with
 * [CycleStartDay.SpecificDayOfMonth] = 5, the cycle for November runs Oct 5 - Nov 4.
 */
class CycleDateCalculator {

    data class Period(val start: LocalDate, val end: LocalDate)

    /**
     * The full period of the cycle that ends in [month], for the given [startDay].
     * Days that don't exist in a month (e.g. the 31st in April) are clamped to its last day.
     */
    fun periodForMonth(startDay: CycleStartDay, month: YearMonth): Period {
        val previousMonth = month.minusMonths(1L)
        return when (startDay) {
            CycleStartDay.FirstDayOfMonth -> Period(
                start = month.atDay(1),
                end = month.atEndOfMonth()
            )

            CycleStartDay.LastDayOfMonth -> Period(
                start = previousMonth.atEndOfMonth(),
                end = month.atEndOfMonth().minusDays(1L)
            )

            is CycleStartDay.SpecificDayOfMonth -> Period(
                start = previousMonth.atClampedDay(startDay.dayOfMonth),
                end = month.atClampedDay(startDay.dayOfMonth).minusDays(1L)
            )
        }
    }

    /**
     * The month whose cycle should follow a cycle that was scheduled to end on
     * [completedCycleScheduledEnd], when the completion is happening on [today].
     *
     * Derived from the *scheduled* end (not the actual completion date) so that completing late
     * doesn't skip a period. If the resulting period already ended before [today] (e.g. the app
     * went unused for months), moves forward to the period that contains [today].
     */
    fun nextCycleMonth(
        startDay: CycleStartDay,
        completedCycleScheduledEnd: LocalDate,
        today: LocalDate
    ): YearMonth {
        var month = YearMonth.from(completedCycleScheduledEnd.plusMonths(1L))
        while (periodForMonth(startDay, month).end.isBefore(today)) {
            month = month.plusMonths(1L)
        }
        return month
    }

    private fun YearMonth.atClampedDay(day: Int): LocalDate =
        atDay(day.coerceIn(1, lengthOfMonth()))
}
