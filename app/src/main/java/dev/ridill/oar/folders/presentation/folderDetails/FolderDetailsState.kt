package dev.ridill.oar.folders.presentation.folderDetails

import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.Empty
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.transactions.domain.model.AggregateAmountItem
import java.time.LocalDateTime
import java.util.Currency

data class FolderDetailsState(
    val folderName: String = String.Empty,
    val createdTimestamp: LocalDateTime = DateUtil.now(),
    val isExcluded: Boolean = false,
    val currency: Currency = LocaleUtil.defaultCurrency,
    val shouldShowActionPreview: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val selectedTransactionIds: Set<Long> = emptySet(),
    val transactionMultiSelectionModeActive: Boolean = false,
    val showMultiSelectionOptions: Boolean = false,
    val aggregatesList: List<AggregateAmountItem> = emptyList(),
    val showAggregates: Boolean = false,
    val showDeleteTransactionsConfirmation: Boolean = false,
    val showRemoveTransactionsConfirmation: Boolean = false
) {
    val createdTimestampFormatted: String
        get() = createdTimestamp.format(DateUtil.Formatters.localizedDateMedium)
}