package dev.ridill.oar.moneyPiles.domain.model

import androidx.annotation.StringRes
import dev.ridill.oar.R

enum class PileHistoryEntryType(
    val icon: String,
    @StringRes val labelRes: Int,
    val isWithdrawal: Boolean = false
) {
    STARTER(
        icon = "🌱",
        labelRes = R.string.pile_history_entry_type_starter
    ),
    AUTO_CONTRIBUTION(
        icon = "🔁",
        labelRes = R.string.pile_history_entry_type_auto_contribution
    ),
    DEPOSIT(
        icon = "↓",
        labelRes = R.string.pile_history_entry_type_deposit
    ),
    WITHDRAWAL(
        icon = "↑",
        labelRes = R.string.pile_history_entry_type_withdrawal,
        isWithdrawal = true
    )
}
