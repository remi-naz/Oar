package dev.ridill.oar.statistics.domain.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.ifInfinite
import dev.ridill.oar.core.domain.util.ifNaN
import dev.ridill.oar.core.ui.util.TextFormat
import java.time.LocalDate
import java.util.Currency
import kotlin.math.absoluteValue

data class StatisticsCycleSummary(
    val cycleId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val spent: Double,
    val received: Double,
    val budget: Long,
    val currency: Currency,
    val transactionCount: Int,
    val daysElapsed: Int,
    val daysTotal: Int
) {
    val description: String
        get() = DateUtil.prettyDateRange(startDate, endDate)

    val budgetFormatted: String
        get() = TextFormat.currency(budget, currency)

    val net: Double
        @Composable get() = remember(spent, received) { spent - received }

    val netFormatted: String
        @Composable get() = TextFormat.currency(net.absoluteValue, currency)

    val usageFraction: Float
        @Composable get() = remember(spent, budget) {
            (spent / budget).ifInfinite { 0.0 }.ifNaN { 0.0 }.toFloat()
        }

    val usagePercent: String
        @Composable get() = TextFormat.percent(usageFraction)

    val paceFraction: Float
        @Composable get() = remember(daysElapsed, daysTotal) {
            (daysElapsed.toFloat() / daysTotal).ifNaN { 0f }
        }

    val projectedSpend: Double
        @Composable get() = if (paceFraction <= 0f) spent else spent / paceFraction

    val isOnPace: Boolean
        @Composable get() = projectedSpend <= budget.toDouble()

    val averagePerDay: Double
        get() = if (daysElapsed <= 0) spent else spent / daysElapsed

    val spentFormatted: String
        get() = TextFormat.currency(spent, currency)

    val receivedFormatted: String
        get() = TextFormat.currency(received, currency)

    val averagePerDayFormatted: String
        get() = TextFormat.currency(averagePerDay, currency)
}
