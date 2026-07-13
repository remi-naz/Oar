package dev.ridill.oar.schedules.presentation.addEditSchedule

import dev.ridill.oar.schedules.domain.model.ScheduleRepetition
import dev.ridill.oar.core.domain.model.FundMovement

interface AddEditScheduleActions {
    fun refreshCurrentDateTime()
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
    fun onOptionClick(option: AddEditScheduleOption)
    fun onDeleteDismiss()
    fun onDeleteConfirm()
    fun onSelectFolderClick()
    fun onRepetitionSelect(repetition: ScheduleRepetition)
    fun onSaveClick()
}
