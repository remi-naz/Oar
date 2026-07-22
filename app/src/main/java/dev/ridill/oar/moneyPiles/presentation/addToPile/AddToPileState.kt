package dev.ridill.oar.moneyPiles.presentation.addToPile

import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails

data class AddToPileState(
    val loading: Boolean = false,
    val pile: MoneyPileDetails? = null,
    val addEnabled: Boolean = false,
)
