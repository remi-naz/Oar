package dev.ridill.oar.transactions.presentation.addEditTransaction

import dev.ridill.oar.schedules.domain.model.ScheduleRepetition
import dev.ridill.oar.transactions.domain.model.TransactionType

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
    fun onTypeChange(type: TransactionType)
    fun onExclusionToggle(excluded: Boolean)
    fun onOptionClick(option: AddEditTxOption)
    fun onDeleteDismiss()
    fun onDeleteConfirm()
    fun onSelectFolderClick()
    fun onRepeatModeClick()
    fun onRepeatModeDismiss()
    fun onRepetitionSelect(repetition: ScheduleRepetition)
    fun onSaveClick()
}