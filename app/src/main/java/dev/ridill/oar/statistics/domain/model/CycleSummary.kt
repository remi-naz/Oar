package dev.ridill.oar.statistics.domain.model

import dev.ridill.oar.core.domain.util.DateUtil
import java.time.LocalDate
import java.util.Currency

data class CycleSummary(
    val cycleId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val spent: Double,
    val received: Double,
    val budget: Long,
    val currency: Currency,
    val aggregate: Double,
    val usageFraction: Float,
    val totalDays: Int,
    val elapsedDays: Int,
) {
    val description: String
        get() = DateUtil.prettyDateRange(startDate, endDate)
}
