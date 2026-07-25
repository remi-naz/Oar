package dev.ridill.oar.moneyPiles.domain.model

import androidx.annotation.StringRes
import dev.ridill.oar.R

enum class PileContributionMode(
    @StringRes val labelRes: Int,
    @StringRes val helpTextRes: Int
) {
    FROM_BALANCE(
        labelRes = R.string.pile_contribution_mode_from_balance,
        helpTextRes = R.string.pile_contribution_mode_from_balance_help
    ),
    TRACK_ONLY(
        labelRes = R.string.pile_contribution_mode_track_only,
        helpTextRes = R.string.pile_contribution_mode_track_only_help
    )
}
