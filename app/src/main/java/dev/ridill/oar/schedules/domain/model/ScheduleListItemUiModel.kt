package dev.ridill.oar.schedules.domain.model

import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.schedules.data.local.entity.ScheduleEntity
import dev.ridill.oar.core.domain.model.FundMovement
import java.time.LocalDateTime
import java.util.Currency

sealed class ScheduleListItemUiModel {
    data class ScheduleItem(
        val id: Long,
        val amount: Double,
        val note: String?,
        val type: FundMovement,
        val currency: Currency,
        val repetition: ScheduleRepetition,
        val lastPaymentTimestamp: LocalDateTime?,
        val nextPaymentTimestamp: LocalDateTime?,
        val canMarkPaid: Boolean
    ) : ScheduleListItemUiModel() {
        constructor(
            scheduleItem: ScheduleEntity,
            canMarkPaid: Boolean
        ) : this(
            id = scheduleItem.id,
            amount = scheduleItem.amount,
            note = scheduleItem.note,
            type = scheduleItem.type,
            currency = LocaleUtil.currencyForCode(scheduleItem.currencyCode),
            repetition = scheduleItem.repetition,
            lastPaymentTimestamp = scheduleItem.lastPaymentTimestamp,
            nextPaymentTimestamp = scheduleItem.nextPaymentTimestamp,
            canMarkPaid = canMarkPaid
        )

        val amountFormatted: String
            get() = TextFormat.currency(amount = amount, currency = currency)
    }

    data class TypeSeparator(val label: UiText) : ScheduleListItemUiModel()
}