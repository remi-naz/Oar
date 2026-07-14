package dev.ridill.oar.moneyPiles.presentation.pileDetails

import dev.ridill.oar.R
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.moneyPiles.domain.model.MoneyPile
import dev.ridill.oar.moneyPiles.domain.model.PileHistoryEntry

data class PileDetailsState(
    val pile: MoneyPile? = null,
    val progressPercent: Int? = null,
    val isGoalReached: Boolean = false,
    val projectedCompletionLabel: UiText = UiText.StringResource(R.string.pile_projection_no_goal),
    val history: List<PileHistoryEntry> = emptyList(),
    val canWithdraw: Boolean = false
)
