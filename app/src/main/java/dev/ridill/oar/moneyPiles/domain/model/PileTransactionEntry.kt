package dev.ridill.oar.moneyPiles.domain.model

import androidx.annotation.StringRes
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.model.FundMovement
import java.time.LocalDateTime

data class PileTransactionEntry(
    val id: Long,
    val amount: Double,
    val movement: FundMovement,
    val contributionSource: ContributionSource,
    val timestamp: LocalDateTime,
)

@get:StringRes
val PileTransactionEntry.labelRes: Int
    get() = when {
        movement == FundMovement.OUT -> R.string.pile_history_entry_type_withdrawal
        contributionSource == ContributionSource.STARTER -> R.string.pile_history_entry_type_starter
        contributionSource == ContributionSource.AUTO -> R.string.pile_history_entry_type_auto_contribution
        else -> R.string.pile_history_entry_type_deposit
    }
