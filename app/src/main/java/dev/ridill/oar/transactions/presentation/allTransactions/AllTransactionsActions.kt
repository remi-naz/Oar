package dev.ridill.oar.transactions.presentation.allTransactions

import dev.ridill.oar.transactions.domain.model.AllTransactionsMultiSelectionOption
import dev.ridill.oar.transactions.domain.model.TransactionTypeFilter

interface AllTransactionsActions {
    fun onSearchClick()
    fun onSearchModeToggle(active: Boolean)
    fun onClearSearchQuery()
    fun onClearAllFiltersClick()
    fun onTypeFilterSelect(filter: TransactionTypeFilter)
    fun onShowExcludedToggle(showExcluded: Boolean)
    fun onTagFilterIdsChange(ids: Set<Long>)
    fun onTransactionLongPress(id: Long)
    fun onTransactionSelectionChange(id: Long)
    fun onDismissMultiSelectionMode()
    fun onMultiSelectionOptionsClick()
    fun onMultiSelectionOptionsDismiss()
    fun onMultiSelectionOptionSelect(option: AllTransactionsMultiSelectionOption)
    fun onDeleteTransactionDismiss()
    fun onDeleteTransactionConfirm()
    fun onAggregationDismiss()
    fun onAggregationConfirm()
    fun onFilterOptionsClick()
    fun onFilterOptionsDismiss()
}