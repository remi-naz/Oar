package dev.ridill.oar.statistics.domain.model

import dev.ridill.oar.core.domain.util.DateUtil
import java.time.LocalDate
import java.time.YearMonth
import java.util.Currency

data class CycleBarEntry(
    val cycleId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val spent: Double,
    val received: Double,
    val budget: Long,
    val currency: Currency
) {
    val label: String
        get() = listOf(startDate, endDate).maxBy { YearMonth.from(it).lengthOfMonth() }
            .format(DateUtil.Formatters.MMM_yy_spaceSep)

    val description: String
        get() = DateUtil.prettyDateRange(startDate, endDate)
}
