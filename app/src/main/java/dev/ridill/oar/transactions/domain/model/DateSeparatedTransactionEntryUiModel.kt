package dev.ridill.oar.transactions.domain.model

import androidx.compose.ui.graphics.Color
import dev.ridill.oar.budgetCycles.domain.model.CycleIndicator
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.moneyPiles.domain.model.ContributionSource
import dev.ridill.oar.moneyPiles.domain.model.PileIcon
import java.time.LocalDateTime
import java.util.Currency

sealed class DateSeparatedTransactionEntryUiModel(
    open val cycle: CycleIndicator,
) {
    data class DateSeparatedTransactionItem(
        val id: Long,
        val note: String,
        val amount: Double,
        val currency: Currency,
        val timestamp: LocalDateTime,
        val type: FundMovement,
        val excluded: Boolean,
        override val cycle: CycleIndicator,
        val tag: TagIndicator?,
        val folder: FolderIndicator?,
        val scheduleId: Long?
    ) : DateSeparatedTransactionEntryUiModel(cycle)

    data class PileContribution(
        val id: Long,
        val pileId: Long,
        val pileName: String,
        val pileColor: Color,
        val pileIcon: PileIcon,
        val movement: FundMovement,
        val timestamp: LocalDateTime,
        val amount: Double,
        val currency: Currency,
        val excluded: Boolean,
        val source: ContributionSource,
        override val cycle: CycleIndicator,
    ) : DateSeparatedTransactionEntryUiModel(cycle)

    data class CycleSeparator(override val cycle: CycleIndicator) :
        DateSeparatedTransactionEntryUiModel(cycle)
}