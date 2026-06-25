package dev.ridill.oar.transactions.presentation.allTransactions

import dev.ridill.oar.core.domain.util.Empty
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.transactions.domain.model.AggregateAmountItem
import dev.ridill.oar.transactions.domain.model.TransactionTypeFilter

data class AllTransactionsState(
    val searchModeActive: Boolean = false,
    val selectedCycleIds: Set<Long> = emptySet(),
    val selectedTransactionTypeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val aggregatesList: List<AggregateAmountItem> = emptyList(),
    val transactionListLabel: UiText = UiText.DynamicString(String.Empty),
    val selectedTransactionIds: Set<Long> = emptySet(),
    val transactionMultiSelectionModeActive: Boolean = false,
    val showDeleteTransactionConfirmation: Boolean = false,
    val showExcludedTransactions: Boolean = false,
    val selectedTagFilterIds: Set<Long> = emptySet(),
    val showAggregationConfirmation: Boolean = false,
    val showMultiSelectionOptions: Boolean = false,
    val showFilterOptions: Boolean = false,
    val showAggregates: Boolean = false
)