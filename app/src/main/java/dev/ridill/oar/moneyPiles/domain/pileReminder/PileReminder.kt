package dev.ridill.oar.moneyPiles.domain.pileReminder

import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails

interface PileReminder {
    fun setReminder(pile: MoneyPileDetails)
    fun cancel(id: Long)

    companion object {
        const val ACTION = "dev.ridill.oar.PILE_REMINDER"
        const val EXTRA_PILE_ID = "EXTRA_PILE_ID"
    }
}
