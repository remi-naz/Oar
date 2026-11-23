package dev.ridill.oar.schedules.domain.util

import com.google.common.truth.Truth.assertThat
import dev.ridill.oar.schedules.domain.model.ScheduleRepetition
import org.junit.Test
import java.time.LocalDateTime

class ScheduleDateCalculatorTest {

    private val calculator = ScheduleDateCalculator()

    @Test
    fun nextPaymentForNoRepeat_isNull() {
        val result = calculator.calculateNextPaymentTimestamp(
            anchor = LocalDateTime.of(2026, 10, 5, 9, 0),
            repetition = ScheduleRepetition.NO_REPEAT,
            expectedTimestamp = LocalDateTime.of(2026, 10, 5, 9, 0)
        )

        assertThat(result).isNull()
    }

    @Test
    fun monthlyPaidLate_advancesToNextMonth() {
        // Regression test: due Oct 5, paid Oct 10. Previously this collapsed back to
        // Oct 31 (same month as the due date) instead of advancing to November.
        val dueDate = LocalDateTime.of(2026, 10, 5, 9, 0)
        val paidOn = LocalDateTime.of(2026, 10, 10, 9, 0)

        val result = calculator.calculateNextPaymentTimestamp(
            anchor = paidOn,
            repetition = ScheduleRepetition.MONTHLY,
            expectedTimestamp = dueDate
        )

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 11, 5, 9, 0))
    }

    @Test
    fun monthlyPaidOnDueDate_advancesOneMonth() {
        val dueDate = LocalDateTime.of(2026, 10, 5, 9, 0)

        val result = calculator.calculateNextPaymentTimestamp(
            anchor = dueDate,
            repetition = ScheduleRepetition.MONTHLY,
            expectedTimestamp = dueDate
        )

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 11, 5, 9, 0))
    }

    @Test
    fun monthlyPaidEarly_ignoresPaymentDate() {
        val dueDate = LocalDateTime.of(2026, 10, 5, 9, 0)
        val paidOn = LocalDateTime.of(2026, 10, 2, 9, 0)

        val result = calculator.calculateNextPaymentTimestamp(
            anchor = paidOn,
            repetition = ScheduleRepetition.MONTHLY,
            expectedTimestamp = dueDate
        )

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 11, 5, 9, 0))
    }

    @Test
    fun monthlyPaidMonthsLate_advancesOnePeriod() {
        val dueDate = LocalDateTime.of(2026, 8, 5, 9, 0)
        val paidOn = LocalDateTime.of(2026, 10, 10, 9, 0)

        val result = calculator.calculateNextPaymentTimestamp(
            anchor = paidOn,
            repetition = ScheduleRepetition.MONTHLY,
            expectedTimestamp = dueDate
        )

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 9, 5, 9, 0))
    }

    @Test
    fun monthlyDueOn31st_clampsInShorterMonth() {
        val dueDate = LocalDateTime.of(2026, 1, 31, 9, 0)

        val result = calculator.calculateNextPaymentTimestamp(
            anchor = dueDate,
            repetition = ScheduleRepetition.MONTHLY,
            expectedTimestamp = dueDate
        )

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 2, 28, 9, 0))
    }

    @Test
    fun monthlyDueOn31st_recoversDayAfterShortMonth() {
        // Regression test for month-end drift: a schedule due on the 31st must not
        // permanently settle on the 28th just because it passed through February.
        val originalDueDate = LocalDateTime.of(2026, 1, 31, 9, 0)

        val februaryOccurrence = calculator.calculateNextPaymentTimestamp(
            anchor = originalDueDate,
            repetition = ScheduleRepetition.MONTHLY,
            expectedTimestamp = originalDueDate,
            originalDueDate = originalDueDate
        )
        assertThat(februaryOccurrence).isEqualTo(LocalDateTime.of(2026, 2, 28, 9, 0))

        val marchOccurrence = calculator.calculateNextPaymentTimestamp(
            anchor = requireNotNull(februaryOccurrence),
            repetition = ScheduleRepetition.MONTHLY,
            expectedTimestamp = februaryOccurrence,
            originalDueDate = originalDueDate
        )

        assertThat(marchOccurrence).isEqualTo(LocalDateTime.of(2026, 3, 31, 9, 0))
    }

    @Test
    fun yearlyDueOnFeb29_recoversNextLeapYear() {
        val originalDueDate = LocalDateTime.of(2024, 2, 29, 9, 0)

        val nonLeapYearOccurrence = calculator.calculateNextPaymentTimestamp(
            anchor = originalDueDate,
            repetition = ScheduleRepetition.YEARLY,
            expectedTimestamp = originalDueDate,
            originalDueDate = originalDueDate
        )
        assertThat(nonLeapYearOccurrence).isEqualTo(LocalDateTime.of(2025, 2, 28, 9, 0))

        val nextLeapYearOccurrence = calculator.calculateNextPaymentTimestamp(
            anchor = requireNotNull(nonLeapYearOccurrence),
            repetition = ScheduleRepetition.YEARLY,
            expectedTimestamp = nonLeapYearOccurrence,
            originalDueDate = originalDueDate
        )
        // 2025 -> 2026 is still non-leap; only 2028 recovers the 29th.
        assertThat(nextLeapYearOccurrence).isEqualTo(LocalDateTime.of(2026, 2, 28, 9, 0))
    }

    @Test
    fun nextPayment_withoutExpectedTimestamp_usesAnchor() {
        val now = LocalDateTime.of(2026, 9, 24, 9, 0)

        val result = calculator.calculateNextPaymentTimestamp(
            anchor = now,
            repetition = ScheduleRepetition.MONTHLY,
            expectedTimestamp = null
        )

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 10, 24, 9, 0))
    }

    @Test
    fun weekly_advancesOneWeekFromDueDate() {
        val dueDate = LocalDateTime.of(2026, 9, 24, 9, 0)
        val paidOn = LocalDateTime.of(2026, 9, 26, 9, 0)

        val result = calculator.calculateNextPaymentTimestamp(
            anchor = paidOn,
            repetition = ScheduleRepetition.WEEKLY,
            expectedTimestamp = dueDate
        )

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 10, 1, 9, 0))
    }

    @Test
    fun biMonthly_advancesTwoMonthsFromDueDate() {
        val dueDate = LocalDateTime.of(2026, 8, 5, 9, 0)
        val paidOn = LocalDateTime.of(2026, 8, 12, 9, 0)

        val result = calculator.calculateNextPaymentTimestamp(
            anchor = paidOn,
            repetition = ScheduleRepetition.BI_MONTHLY,
            expectedTimestamp = dueDate
        )

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 10, 5, 9, 0))
    }

    @Test
    fun yearly_advancesOneYearFromDueDate() {
        val dueDate = LocalDateTime.of(2026, 3, 15, 9, 0)
        val paidOn = LocalDateTime.of(2026, 3, 20, 9, 0)

        val result = calculator.calculateNextPaymentTimestamp(
            anchor = paidOn,
            repetition = ScheduleRepetition.YEARLY,
            expectedTimestamp = dueDate
        )

        assertThat(result).isEqualTo(LocalDateTime.of(2027, 3, 15, 9, 0))
    }

    @Test
    fun lastPaymentForNoRepeat_isNull() {
        val result = calculator.calculateLastPaymentTimestamp(
            anchor = LocalDateTime.of(2026, 10, 5, 9, 0),
            repetition = ScheduleRepetition.NO_REPEAT,
            expectedTimestamp = LocalDateTime.of(2026, 10, 5, 9, 0)
        )

        assertThat(result).isNull()
    }

    @Test
    fun monthlyLastPayment_stepsBackOneMonth() {
        val dueDate = LocalDateTime.of(2026, 10, 5, 9, 0)

        val result = calculator.calculateLastPaymentTimestamp(
            anchor = LocalDateTime.of(2026, 10, 10, 9, 0),
            repetition = ScheduleRepetition.MONTHLY,
            expectedTimestamp = dueDate
        )

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 9, 5, 9, 0))
    }

    @Test
    fun biMonthlyLastPayment_stepsBackTwoMonths() {
        val dueDate = LocalDateTime.of(2026, 10, 5, 9, 0)

        val result = calculator.calculateLastPaymentTimestamp(
            anchor = LocalDateTime.of(2026, 10, 10, 9, 0),
            repetition = ScheduleRepetition.BI_MONTHLY,
            expectedTimestamp = dueDate
        )

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 8, 5, 9, 0))
    }
}
