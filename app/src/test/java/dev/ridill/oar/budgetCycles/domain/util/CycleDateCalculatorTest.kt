package dev.ridill.oar.budgetCycles.domain.util

import com.google.common.truth.Truth.assertThat
import dev.ridill.oar.budgetCycles.domain.model.CycleStartDay
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class CycleDateCalculatorTest {

    private val calculator = CycleDateCalculator()

    // region periodForMonth

    @Test
    fun `first day of month period spans the calendar month`() {
        val period = calculator.periodForMonth(
            CycleStartDay.FirstDayOfMonth,
            YearMonth.of(2026, 11)
        )

        assertThat(period.start).isEqualTo(LocalDate.of(2026, 11, 1))
        assertThat(period.end).isEqualTo(LocalDate.of(2026, 11, 30))
    }

    @Test
    fun `last day of month period starts previous month end and ends day before this month end`() {
        val period = calculator.periodForMonth(
            CycleStartDay.LastDayOfMonth,
            YearMonth.of(2026, 11)
        )

        assertThat(period.start).isEqualTo(LocalDate.of(2026, 10, 31))
        assertThat(period.end).isEqualTo(LocalDate.of(2026, 11, 29))
    }

    @Test
    fun `specific day period runs from that day to the day before it next month`() {
        val period = calculator.periodForMonth(
            CycleStartDay.SpecificDayOfMonth(5),
            YearMonth.of(2026, 11)
        )

        assertThat(period.start).isEqualTo(LocalDate.of(2026, 10, 5))
        assertThat(period.end).isEqualTo(LocalDate.of(2026, 11, 4))
    }

    @Test
    fun `january period reaches back into previous year without crashing`() {
        val specific = calculator.periodForMonth(
            CycleStartDay.SpecificDayOfMonth(10),
            YearMonth.of(2027, 1)
        )
        val last = calculator.periodForMonth(
            CycleStartDay.LastDayOfMonth,
            YearMonth.of(2027, 1)
        )

        assertThat(specific.start).isEqualTo(LocalDate.of(2026, 12, 10))
        assertThat(specific.end).isEqualTo(LocalDate.of(2027, 1, 9))
        assertThat(last.start).isEqualTo(LocalDate.of(2026, 12, 31))
        assertThat(last.end).isEqualTo(LocalDate.of(2027, 1, 30))
    }

    @Test
    fun `specific day 31 is clamped in short months`() {
        val april = calculator.periodForMonth(
            CycleStartDay.SpecificDayOfMonth(31),
            YearMonth.of(2026, 4)
        )
        val march = calculator.periodForMonth(
            CycleStartDay.SpecificDayOfMonth(31),
            YearMonth.of(2026, 3)
        )

        // Mar 31 -> Apr 30 clamped, minus a day
        assertThat(april.start).isEqualTo(LocalDate.of(2026, 3, 31))
        assertThat(april.end).isEqualTo(LocalDate.of(2026, 4, 29))
        // Feb 28 clamped -> Mar 31, minus a day
        assertThat(march.start).isEqualTo(LocalDate.of(2026, 2, 28))
        assertThat(march.end).isEqualTo(LocalDate.of(2026, 3, 30))
    }

    @Test
    fun `specific day 29 to 30 are clamped in february`() {
        val leapFeb = calculator.periodForMonth(
            CycleStartDay.SpecificDayOfMonth(30),
            YearMonth.of(2028, 2)
        )
        val nonLeapFeb = calculator.periodForMonth(
            CycleStartDay.SpecificDayOfMonth(29),
            YearMonth.of(2026, 2)
        )

        assertThat(leapFeb.end).isEqualTo(LocalDate.of(2028, 2, 28))
        assertThat(nonLeapFeb.end).isEqualTo(LocalDate.of(2026, 2, 27))
    }

    // endregion

    // region nextCycleMonth

    private val firstDay = CycleStartDay.FirstDayOfMonth

    @Test
    fun `on time completion moves to next month`() {
        val result = calculator.nextCycleMonth(
            startDay = firstDay,
            completedCycleScheduledEnd = LocalDate.of(2026, 10, 31),
            today = LocalDate.of(2026, 11, 1)
        )

        assertThat(result).isEqualTo(YearMonth.of(2026, 11))
    }

    @Test
    fun `early manual completion still moves to next month`() {
        val result = calculator.nextCycleMonth(
            startDay = firstDay,
            completedCycleScheduledEnd = LocalDate.of(2026, 10, 31),
            today = LocalDate.of(2026, 10, 20)
        )

        assertThat(result).isEqualTo(YearMonth.of(2026, 11))
    }

    @Test
    fun `late completion does not skip a period`() {
        // Regression: completing on Nov 4 used to derive December from "yesterday + 1 month"
        val result = calculator.nextCycleMonth(
            startDay = firstDay,
            completedCycleScheduledEnd = LocalDate.of(2026, 10, 31),
            today = LocalDate.of(2026, 11, 4)
        )

        assertThat(result).isEqualTo(YearMonth.of(2026, 11))
    }

    @Test
    fun `months of inactivity jump to the period containing today`() {
        val result = calculator.nextCycleMonth(
            startDay = firstDay,
            completedCycleScheduledEnd = LocalDate.of(2026, 10, 31),
            today = LocalDate.of(2027, 2, 10)
        )

        assertThat(result).isEqualTo(YearMonth.of(2027, 2))
    }

    @Test
    fun `specific day late completion stays within the following period`() {
        val startDay = CycleStartDay.SpecificDayOfMonth(5)
        // Cycle Oct 5 - Nov 4 completed late on Nov 8 -> next is Nov 5 - Dec 4 (December)
        val result = calculator.nextCycleMonth(
            startDay = startDay,
            completedCycleScheduledEnd = LocalDate.of(2026, 11, 4),
            today = LocalDate.of(2026, 11, 8)
        )

        assertThat(result).isEqualTo(YearMonth.of(2026, 12))
    }

    @Test
    fun `last day of month late completion stays within the following period`() {
        val startDay = CycleStartDay.LastDayOfMonth
        // Cycle Oct 31 - Nov 29 completed on Dec 2 -> next is Nov 30 - Dec 30 (December)
        val result = calculator.nextCycleMonth(
            startDay = startDay,
            completedCycleScheduledEnd = LocalDate.of(2026, 11, 29),
            today = LocalDate.of(2026, 12, 2)
        )

        assertThat(result).isEqualTo(YearMonth.of(2026, 12))
    }

    @Test
    fun `year boundary advances to january of next year`() {
        val result = calculator.nextCycleMonth(
            startDay = firstDay,
            completedCycleScheduledEnd = LocalDate.of(2026, 12, 31),
            today = LocalDate.of(2027, 1, 3)
        )

        assertThat(result).isEqualTo(YearMonth.of(2027, 1))
    }

    // endregion
}
