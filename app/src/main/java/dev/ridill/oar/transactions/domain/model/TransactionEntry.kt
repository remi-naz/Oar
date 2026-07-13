package dev.ridill.oar.transactions.domain.model

import androidx.compose.ui.graphics.Color
import dev.ridill.oar.budgetCycles.domain.model.CycleIndicator
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.ui.util.TextFormat
import java.time.LocalDateTime
import java.util.Currency

data class TransactionEntry(
    val id: Long,
    val note: String,
    val amount: Double,
    val timestamp: LocalDateTime,
    val type: FundMovement,
    val excluded: Boolean,
    val cycle: CycleIndicator,
    val tag: TagIndicator?,
    val folder: FolderIndicator?,
    val scheduleId: Long?,
    val currency: Currency
) {
    val amountFormatted: String
        get() = TextFormat.currency(
            amount = amount,
            currency = currency
        )
}

data class TagIndicator(
    val id: Long,
    val name: String,
    val color: Color
)

data class FolderIndicator(
    val id: Long,
    val name: String,
)