package dev.ridill.oar.moneyPiles.domain.model

import androidx.annotation.StringRes
import dev.ridill.oar.R

enum class ContributionSource(
    @StringRes val labelRes: Int,
) {
    STARTER(labelRes = R.string.pile_history_entry_type_starter),
    AUTO(labelRes = R.string.pile_history_entry_type_auto_contribution),
    MANUAL(labelRes = R.string.pile_history_entry_type_manual),
    SWEEP_OUT(labelRes = R.string.pile_history_entry_type_sweep_out)
}