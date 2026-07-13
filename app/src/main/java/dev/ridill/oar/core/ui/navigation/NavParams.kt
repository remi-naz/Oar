package dev.ridill.oar.core.ui.navigation

import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.schedules.domain.model.Schedule
import dev.ridill.oar.schedules.domain.model.ScheduleRepetition
import dev.ridill.oar.transactions.domain.model.Transaction
import dev.ridill.oar.core.domain.model.FundMovement
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleInputs(
    val amount: Double,
    val note: String,
    val type: FundMovement,
    val currencyCode: String,
    val folderId: Long?,
    val tagId: Long?,
)

fun Transaction.toScheduleInputs(): ScheduleInputs = ScheduleInputs(
    amount = amount.toDoubleOrNull().orZero(),
    note = note,
    type = type,
    currencyCode = currency.currencyCode,
    folderId = folderId,
    tagId = tagId
)

fun ScheduleInputs.toSchedule(): Schedule = Schedule(
    id = OarDatabase.DEFAULT_ID_LONG,
    amount = amount,
    note = note,
    currency = LocaleUtil.currencyForCode(currencyCode),
    type = type,
    tagId = tagId,
    folderId = folderId,
    repetition = ScheduleRepetition.NO_REPEAT,
    nextPaymentTimestamp = null,
    lastPaymentTimestamp = null
)