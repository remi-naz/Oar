package dev.ridill.oar.moneyPiles.presentation.movePileFund

import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails

data class MovePileFundState(
    val loading: Boolean = false,
    val pile: MoneyPileDetails? = null,
    val addEnabled: Boolean = false,
)
