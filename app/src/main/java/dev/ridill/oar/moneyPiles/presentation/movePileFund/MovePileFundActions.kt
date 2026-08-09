package dev.ridill.oar.moneyPiles.presentation.movePileFund

interface MovePileFundActions {
    fun onAddRecommendedAmountClick()
    fun onTimestampClick()
    fun onDateSelectionDismiss()
    fun onDateSelectionConfirm(millis: Long)
    fun onPickTimeClick()
    fun onTimeSelectionDismiss()
    fun onTimeSelectionConfirm(hour: Int, minute: Int)
    fun onPickDateClick()
    fun onConfirm()
}
