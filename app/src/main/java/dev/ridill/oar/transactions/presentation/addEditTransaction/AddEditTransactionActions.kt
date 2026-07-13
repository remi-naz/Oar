package dev.ridill.oar.transactions.presentation.addEditTransaction

import dev.ridill.oar.core.domain.model.FundMovement

interface AddEditTransactionActions {
    fun onAmountFocusLost()
    fun onEvaluateExpressionClick()
    fun onRecommendedAmountClick(amount: Long)
    fun onTagSelect(tagId: Long?)
    fun onTimestampClick()
    fun onDateSelectionDismiss()
    fun onDateSelectionConfirm(millis: Long)
    fun onPickTimeClick()
    fun onTimeSelectionDismiss()
    fun onTimeSelectionConfirm(hour: Int, minute: Int)
    fun onPickDateClick()
    fun onTypeChange(type: FundMovement)
    fun onExclusionToggle(excluded: Boolean)
    fun onOptionClick(option: AddEditTxOption)
    fun onDeleteDismiss()
    fun onDeleteConfirm()
    fun onSelectFolderClick()
    fun onSaveClick()
}