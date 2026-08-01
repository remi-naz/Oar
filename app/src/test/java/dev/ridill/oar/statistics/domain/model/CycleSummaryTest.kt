package dev.ridill.oar.statistics.domain.model

import com.google.common.truth.Truth.assertThat
import dev.ridill.oar.core.domain.util.LocaleUtil
import org.junit.Test
import java.time.LocalDate

class CycleSummaryTest {

    private val currency = LocaleUtil.currencyForCode("INR")

    private fun summary(
        spent: Double = 5_000.0,
        received: Double = 0.0,
        budget: Long = 10_000L,
        transactionCount: Int = 10,
        daysElapsed: Int = 15,
        daysTotal: Int = 30
    ) = CycleSummary(
        cycleId = 1L,
        startDate = LocalDate.of(2026, 1, 1),
        endDate = LocalDate.of(2026, 1, 30),
        spent = spent,
        received = received,
        budget = budget,
        currency = currency,
        transactionCount = transactionCount,
        daysElapsed = daysElapsed,
        daysTotal = daysTotal
    )

    @Test
    fun testUsageFraction_matchesSpentOverBudget() {
        val result = summary(spent = 2_500.0, budget = 10_000L)
        assertThat(result.usageFraction).isWithin(0.0001f).of(0.25f)
    }

    @Test
    fun testUsageFraction_withZeroBudget_isZeroNotNaN() {
        val result = summary(spent = 2_500.0, budget = 0L)
        assertThat(result.usageFraction).isEqualTo(0f)
    }

    @Test
    fun testProjectedSpend_atHalfCycleElapsed_doublesCurrentSpend() {
        val result = summary(spent = 5_000.0, daysElapsed = 15, daysTotal = 30)
        assertThat(result.projectedSpend).isWithin(0.01).of(10_000.0)
    }

    @Test
    fun testProjectedSpend_withZeroDaysElapsed_fallsBackToSpentInsteadOfInfinite() {
        val result = summary(spent = 5_000.0, daysElapsed = 0, daysTotal = 30)
        assertThat(result.projectedSpend).isEqualTo(5_000.0)
    }

    @Test
    fun testIsOnPace_whenProjectedSpendEqualsBudget_isTrue() {
        val result = summary(spent = 5_000.0, budget = 10_000L, daysElapsed = 15, daysTotal = 30)
        assertThat(result.isOnPace).isTrue()
    }

    @Test
    fun testIsOnPace_whenProjectedSpendExceedsBudget_isFalse() {
        val result = summary(spent = 6_000.0, budget = 10_000L, daysElapsed = 15, daysTotal = 30)
        assertThat(result.isOnPace).isFalse()
    }

    @Test
    fun testAveragePerDay_withZeroDaysElapsed_fallsBackToSpentInsteadOfInfinite() {
        val result = summary(spent = 5_000.0, daysElapsed = 0)
        assertThat(result.averagePerDay).isEqualTo(5_000.0)
    }

    @Test
    fun testAveragePerDay_dividesSpentByDaysElapsed() {
        val result = summary(spent = 3_000.0, daysElapsed = 3)
        assertThat(result.averagePerDay).isWithin(0.01).of(1_000.0)
    }

    @Test
    fun testNet_isSpentMinusReceived() {
        val result = summary(spent = 5_000.0, received = 1_200.0)
        assertThat(result.net).isWithin(0.01).of(3_800.0)
    }
}
