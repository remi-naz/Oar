package dev.ridill.oar.schedules.presentation.addEditSchedule

import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.schedules.domain.model.ScheduleRepetition
import dev.ridill.oar.core.domain.model.FundMovement
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Currency

data class AddEditScheduleState(
    val menuOptions: Set<AddEditScheduleOption> = emptySet(),
    val currency: Currency = LocaleUtil.defaultCurrency,
    val isLoading: Boolean = false,
    val fundMovement: FundMovement = FundMovement.OUT,
    val isAmountInputAnExpression: Boolean = false,
    val amountRecommendations: List<Long> = emptyList(),
    val timestamp: LocalDateTime = DateUtil.now(),
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val selectedTagId: Long? = null,
    val showDeleteConfirmation: Boolean = false,
    val linkedFolderName: String? = null,
    val selectedRepetition: ScheduleRepetition = ScheduleRepetition.NO_REPEAT,
) {
    val timestampUtc: ZonedDateTime
        get() = timestamp.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC)
}
