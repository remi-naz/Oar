package dev.ridill.oar.moneyPiles.presentation.sweepout

interface PileSweepOutActions {
    fun refreshTimestampNow()
    fun onCreateLinkedTransactionToggle(checked: Boolean)
    fun onConfirm()
}
