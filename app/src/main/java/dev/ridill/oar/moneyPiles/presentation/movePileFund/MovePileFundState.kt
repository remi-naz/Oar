package dev.ridill.oar.moneyPiles.presentation.movePileFund

import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

data class MovePileFundState(
    val loading: Boolean = false,
    val pile: MoneyPileDetails? = null,
    val timestamp: LocalDateTime = DateUtil.now(),
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val cycleDescription: String? = null,
    val selectedCycleId: Long? = null,
) {
    val timestampUtc: ZonedDateTime
        get() = timestamp.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC)
}
