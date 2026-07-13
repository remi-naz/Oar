package dev.ridill.oar.schedules.domain.model

import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.core.domain.model.FundMovement
import java.time.LocalDateTime
import java.util.Currency

data class ActiveSchedule(
    val id: Long,
    val note: String?,
    val amount: Double,
    val currency: Currency,
    val type: FundMovement,
    val nextPaymentDateTime: LocalDateTime
) {
    val amountFormatted: String
        get() = TextFormat.currency(amount, currency)

    val dayFormatted: String
        get() = nextPaymentDateTime.format(DateUtil.Formatters.dayOfMonthOrdinal)
}