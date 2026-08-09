package dev.ridill.oar.moneyPiles.presentation.pileDetails

interface PileDetailActions {
    fun onTransactionActionRevealed()
    fun onTransactionDelete(id: Long)
    fun onIncludeLockedTransactionsToggle(includeLocked: Boolean)
    fun onDeletePileClick()
    fun onDeletePileConfirmationDismiss()
    fun onDeletePileConfirm()
}
