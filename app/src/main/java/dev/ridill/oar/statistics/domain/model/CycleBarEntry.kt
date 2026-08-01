package dev.ridill.oar.statistics.domain.model

import dev.ridill.oar.core.domain.util.DateUtil
import java.time.LocalDate

data class CycleBarEntry(
    val cycleId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val spent: Double,
    val received: Double,
    val budget: Long
) {
    val label: String
        get() = startDate.format(DateUtil.Formatters.MMM)

    val description: String
        get() = DateUtil.prettyDateRange(startDate, endDate)
}
