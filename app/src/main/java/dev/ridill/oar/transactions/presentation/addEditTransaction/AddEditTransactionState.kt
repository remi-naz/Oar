package dev.ridill.oar.transactions.presentation.addEditTransaction

import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.model.FundMovement
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Currency

data class AddEditTransactionState(
    val menuOptions: Set<AddEditTxOption> = emptySet(),
    val currency: Currency = LocaleUtil.defaultCurrency,
    val isLoading: Boolean = false,
    val fundMovement: FundMovement = FundMovement.OUT,
    val isAmountInputAnExpression: Boolean = false,
    val amountRecommendations: List<Long> = emptyList(),
    val timestamp: LocalDateTime = DateUtil.now(),
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val isTransactionExcluded: Boolean = false,
    val selectedTagId: Long? = null,
    val showDeleteConfirmation: Boolean = false,
    val linkedFolderName: String? = null,
    val cycleDescription: String? = null,
    val selectedCycleId: Long? = null,
) {
    val timestampUtc: ZonedDateTime
        get() = timestamp.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC)
}