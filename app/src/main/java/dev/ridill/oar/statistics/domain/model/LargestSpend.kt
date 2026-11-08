package dev.ridill.oar.statistics.domain.model

import dev.ridill.oar.core.ui.util.TextFormat
import java.time.LocalDateTime
import java.util.Currency

data class LargestSpend(
    val amount: Double,
    val note: String,
    val tagName: String?,
    val folderName: String?,
    val timestamp: LocalDateTime,
    val currency: Currency
) {
    val amountFormatted: String
        get() = TextFormat.currency(amount, currency)
}
