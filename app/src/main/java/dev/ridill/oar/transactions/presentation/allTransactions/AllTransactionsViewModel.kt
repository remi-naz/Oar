package dev.ridill.oar.transactions.presentation.allTransactions

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.aggregations.domain.repository.AggregationsRepository
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.core.domain.util.addOrRemove
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.domain.util.textAsFlow
import dev.ridill.oar.core.ui.navigation.AddEditScheduleResult
import dev.ridill.oar.core.ui.navigation.AddEditTxResult
import dev.ridill.oar.core.ui.navigation.INVALID_ID_LONG
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.transactions.domain.model.AllTransactionsMultiSelectionOption
import dev.ridill.oar.transactions.domain.model.TransactionTypeFilter
import dev.ridill.oar.transactions.domain.repository.AllTransactionsRepository
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllTransactionsViewModel @Inject constructor(
    private val transactionRepo: AllTransactionsRepository,
    private val aggregatesRepo: AggregationsRepository,
    private val savedStateHandle: SavedStateHandle,
    private val eventBus: EventBus<AllTransactionsEvent>
) : ViewModel(), AllTransactionsActions {

    private val searchModeActive = savedStateHandle.getStateFlow(SEARCH_MODE_ACTIVE, false)
    val searchQueryState = savedStateHandle.saveable(
        key = "SEARCH_QUERY_STATE",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )
    private val selectedCycleIds = savedStateHandle
        .getStateFlow<Set<Long>>(SELECTED_CYCLE_IDS, emptySet())

    private val transactionTypeFilter = savedStateHandle
        .getStateFlow(TRANSACTION_TYPE_FILTER, TransactionTypeFilter.ALL)

    private val showExcludedTransactions = transactionRepo.getShowExcludedOption()

    private val selectedTagIds = savedStateHandle
        .getStateFlow<Set<Long>>(SELECTED_TAG_IDS, emptySet())

    private val selectedTransactionIds = savedStateHandle
        .getStateFlow<Set<Long>>(SELECTED_TRANSACTION_IDS, emptySet())
    private val transactionMultiSelectionModeActive = selectedTransactionIds
        .mapLatest { it.isNotEmpty() }
        .distinctUntilChanged()

    val transactionsPagingData = combineTuple(
        selectedCycleIds,
        transactionTypeFilter,
        showExcludedTransactions,
        selectedTagIds
    ).flatMapLatest { (
                          cycleIds,
                          typeFilter,
                          showExcluded,
                          tagIds
                      ) ->
        transactionRepo.getAllTransactionsPaged(
            cycleIds = cycleIds,
            transactionType = TransactionTypeFilter.mapToTransactionType(typeFilter),
            showExcluded = showExcluded,
            tagIds = tagIds
        )
    }.cachedIn(viewModelScope)

    val searchResults = searchQueryState.textAsFlow()
        .debounce(UtilConstants.DebounceTimeoutDuration)
        .flatMapLatest { query ->
            transactionRepo.getSearchResults(query)
        }.cachedIn(viewModelScope)

    private val aggregatesList = selectedTransactionIds.flatMapLatest { selectedTxIds ->
        aggregatesRepo.getAmountAggregateForTransactions(
            selectedTxIds = selectedTxIds,
            addExcluded = true
        )
    }.distinctUntilChanged()

    private val showAggregates = aggregatesList
        .mapLatest { it.isNotEmpty() }
        .distinctUntilChanged()

    private val transactionListLabel = transactionTypeFilter.mapLatest { type ->
        when {
            type != TransactionTypeFilter.ALL -> UiText.StringResource(type.labelRes)
            else -> UiText.StringResource(R.string.all_transactions)
        }
    }.distinctUntilChanged()

    private val showDeleteTransactionConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_TRANSACTION_CONFIRMATION, false)

    private val showAggregationConfirmation = savedStateHandle
        .getStateFlow(SHOW_AGGREGATION_CONFIRMATION, false)

    private val showMultiSelectionOptions = savedStateHandle
        .getStateFlow(SHOW_MULTI_SELECTION_OPTIONS, false)

    private val showFilterOptions = savedStateHandle
        .getStateFlow(SHOW_FILTER_OPTIONS, false)

    val state = combineTuple(
        searchModeActive,
        selectedCycleIds,
        transactionTypeFilter,
        aggregatesList,
        transactionListLabel,
        selectedTransactionIds,
        transactionMultiSelectionModeActive,
        showDeleteTransactionConfirmation,
        showExcludedTransactions,
        showAggregationConfirmation,
        showMultiSelectionOptions,
        showFilterOptions,
        selectedTagIds,
        showAggregates
    ).mapLatest { (
                      searchModeActive,
                      selectedCycleIds,
                      transactionTypeFilter,
                      aggregatesList,
                      transactionListLabel,
                      selectedTransactionIds,
                      transactionMultiSelectionModeActive,
                      showDeleteTransactionConfirmation,
                      showExcludedTransactions,
                      showAggregationConfirmation,
                      showMultiSelectionOptions,
                      showFilterOptions,
                      selectedTagIds,
                      showAggregates
                  ) ->
        AllTransactionsState(
            searchModeActive = searchModeActive,
            selectedCycleIds = selectedCycleIds,
            selectedTransactionTypeFilter = transactionTypeFilter,
            aggregatesList = aggregatesList,
            transactionListLabel = transactionListLabel,
            selectedTransactionIds = selectedTransactionIds,
            transactionMultiSelectionModeActive = transactionMultiSelectionModeActive,
            showDeleteTransactionConfirmation = showDeleteTransactionConfirmation,
            showExcludedTransactions = showExcludedTransactions,
            showAggregationConfirmation = showAggregationConfirmation,
            showMultiSelectionOptions = showMultiSelectionOptions,
            showFilterOptions = showFilterOptions,
            selectedTagFilterIds = selectedTagIds,
            showAggregates = showAggregates
        )
    }.asStateFlow(viewModelScope, AllTransactionsState())

    val events = eventBus.eventFlow

    override fun onSearchClick() {
        savedStateHandle[SEARCH_MODE_ACTIVE] = true
    }

    override fun onSearchModeToggle(active: Boolean) {
        savedStateHandle[SEARCH_MODE_ACTIVE] = active
        if (!active) {
            searchQueryState.clearText()
        }
    }

    override fun onClearSearchQuery() {
        searchQueryState.clearText()
    }

    override fun onClearAllFiltersClick() {
        savedStateHandle[SELECTED_CYCLE_IDS] = emptySet<Long>()
        savedStateHandle[TRANSACTION_TYPE_FILTER] = TransactionTypeFilter.ALL
        savedStateHandle[SELECTED_TAG_IDS] = emptySet<Long>()
        savedStateHandle[SHOW_FILTER_OPTIONS] = false
    }

    override fun onTypeFilterSelect(filter: TransactionTypeFilter) {
        savedStateHandle[TRANSACTION_TYPE_FILTER] = filter
    }

    override fun onShowExcludedToggle(showExcluded: Boolean) {
        viewModelScope.launch {
            transactionRepo.toggleShowExcludedOption(showExcluded)
        }
    }

    override fun onTransactionLongPress(id: Long) {
        savedStateHandle[SELECTED_TRANSACTION_IDS] = selectedTransactionIds.value + id
    }

    override fun onTransactionSelectionChange(id: Long) {
        savedStateHandle[SELECTED_TRANSACTION_IDS] = selectedTransactionIds.value.addOrRemove(id)
    }

    override fun onDismissMultiSelectionMode() {
        dismissMultiSelectionMode()
    }

    private fun dismissMultiSelectionMode() {
        savedStateHandle[SELECTED_TRANSACTION_IDS] = emptySet<Long>()
    }

    override fun onMultiSelectionOptionsClick() {
        savedStateHandle[SHOW_MULTI_SELECTION_OPTIONS] = true
    }

    override fun onMultiSelectionOptionsDismiss() {
        savedStateHandle[SHOW_MULTI_SELECTION_OPTIONS] = false
    }

    override fun onMultiSelectionOptionSelect(option: AllTransactionsMultiSelectionOption) {
        savedStateHandle[SHOW_MULTI_SELECTION_OPTIONS] = false
        val selectedTransactionIds = selectedTransactionIds.value.ifEmpty { return }
        viewModelScope.launch {
            when (option) {
                AllTransactionsMultiSelectionOption.DELETE -> {
                    savedStateHandle[SHOW_DELETE_TRANSACTION_CONFIRMATION] = true
                }

                AllTransactionsMultiSelectionOption.ASSIGN_TAG -> {
                    eventBus.send(AllTransactionsEvent.NavigateToTagSelectionForAssignment)
                }

                AllTransactionsMultiSelectionOption.REMOVE_TAG -> {
                    removeTagForTransactions(selectedTransactionIds)
                }

                AllTransactionsMultiSelectionOption.EXCLUDE_FROM_EXPENDITURE -> {
                    toggleTransactionExclusion(selectedTransactionIds, true)
                }

                AllTransactionsMultiSelectionOption.INCLUDE_IN_EXPENDITURE -> {
                    toggleTransactionExclusion(selectedTransactionIds, false)
                }

                AllTransactionsMultiSelectionOption.ADD_TO_FOLDER -> {
                    showFolderSelection()
                }

                AllTransactionsMultiSelectionOption.REMOVE_FROM_FOLDERS -> {
                    removeTransactionsFromFolders(selectedTransactionIds)
                }

                AllTransactionsMultiSelectionOption.AGGREGATE_TOGETHER -> {
                    savedStateHandle[SHOW_AGGREGATION_CONFIRMATION] = true
                }

                AllTransactionsMultiSelectionOption.CHANGE_CYCLE -> {
                    eventBus.send(AllTransactionsEvent.ChooseCycleForTransactions)
                }
            }
        }
    }

    fun onTagAssignmentSelectionResult(tagId: Long?) {
        if (tagId != null) viewModelScope.launch {
            assignTagToTransactions(tagId)
        }
    }

    private suspend fun assignTagToTransactions(selectedId: Long) {
        transactionRepo.setTagIdToTransactions(selectedId, selectedTransactionIds.value)
        dismissMultiSelectionMode()
        eventBus.send(AllTransactionsEvent.ShowUiMessage(UiText.StringResource(R.string.tag_assigned_to_transactions)))
    }

    private suspend fun removeTagForTransactions(ids: Set<Long>) {
        transactionRepo.setTagIdToTransactions(null, ids)
        dismissMultiSelectionMode()
        eventBus.send(AllTransactionsEvent.ShowUiMessage(UiText.StringResource(R.string.tag_removed_from_transactions)))
    }

    private suspend fun toggleTransactionExclusion(ids: Set<Long>, excluded: Boolean) {
        transactionRepo.toggleTransactionExclusionByIds(
            ids = ids,
            excluded = excluded
        )
        dismissMultiSelectionMode()
        eventBus.send(
            AllTransactionsEvent.ShowUiMessage(
                UiText.StringResource(
                    if (excluded) R.string.transactions_excluded_from_expenditure
                    else R.string.transactions_included_in_expenditure
                )
            )
        )
    }

    override fun onTagFilterIdsChange(ids: Set<Long>) {
        savedStateHandle[SELECTED_TAG_IDS] = ids
    }

    private suspend fun showFolderSelection() {
        eventBus.send(AllTransactionsEvent.NavigateToFolderSelection)
    }

    private suspend fun removeTransactionsFromFolders(ids: Set<Long>) {
        transactionRepo.removeTransactionsFromFolders(ids)
        dismissMultiSelectionMode()
        eventBus.send(AllTransactionsEvent.ShowUiMessage(UiText.StringResource(R.string.transactions_removed_from_their_folders)))
    }

    fun onFolderSelect(folderId: Long) {
        if (folderId == INVALID_ID_LONG) return
        viewModelScope.launch {
            val selectedIds = selectedTransactionIds.value
            transactionRepo.addTransactionsToFolderByIds(
                ids = selectedIds,
                folderId = folderId
            )
            eventBus.send(
                AllTransactionsEvent.ShowUiMessage(
                    UiText.PluralResource(
                        R.plurals.transaction_added_to_folder_message,
                        selectedIds.size
                    )
                )
            )
            dismissMultiSelectionMode()
        }
    }

    fun onAddEditTxNavResult(result: AddEditTxResult) = viewModelScope.launch {
        val event = when (result) {
            AddEditTxResult.TRANSACTION_DELETED ->
                AllTransactionsEvent.ShowUiMessage(
                    UiText.PluralResource(
                        R.plurals.transaction_deleted,
                        1
                    )
                )

            AddEditTxResult.TRANSACTION_SAVED ->
                AllTransactionsEvent.ShowUiMessage(UiText.StringResource(R.string.transaction_saved))
        }

        eventBus.send(event)
    }

    fun onAddEditScheduleNavResult(result: AddEditScheduleResult) = viewModelScope.launch {
        if (result == AddEditScheduleResult.SCHEDULE_SAVED) {
            eventBus.send(AllTransactionsEvent.ScheduleSaved)
        }
    }

    override fun onDeleteTransactionDismiss() {
        savedStateHandle[SHOW_DELETE_TRANSACTION_CONFIRMATION] = false
    }

    override fun onDeleteTransactionConfirm() {
        val selectedIds = selectedTransactionIds.value
        if (selectedIds.isEmpty()) {
            savedStateHandle[SHOW_DELETE_TRANSACTION_CONFIRMATION] = false
            return
        }
        viewModelScope.launch {
            deleteTransactions(selectedIds)
            savedStateHandle[SHOW_DELETE_TRANSACTION_CONFIRMATION] = false
            dismissMultiSelectionMode()
        }
    }

    private suspend fun deleteTransactions(ids: Set<Long>) {
        transactionRepo.deleteTransactionsByIds(ids)
        eventBus.send(AllTransactionsEvent.ShowUiMessage(UiText.StringResource(R.string.transactions_deleted)))
    }

    override fun onAggregationDismiss() {
        savedStateHandle[SHOW_AGGREGATION_CONFIRMATION] = false
    }

    override fun onAggregationConfirm() {
        viewModelScope.launch {
            val selectedIds = selectedTransactionIds.value
            val dateTimeNow = DateUtil.now()
            val insertedId = transactionRepo.aggregateTogether(
                ids = selectedIds,
                dateTime = dateTimeNow
            )
            savedStateHandle[SHOW_AGGREGATION_CONFIRMATION] = false
            dismissMultiSelectionMode()
            eventBus.send(AllTransactionsEvent.NavigateToAddEditTx(insertedId))
        }
    }

    override fun onFilterOptionsClick() {
        savedStateHandle[SHOW_FILTER_OPTIONS] = true
    }

    override fun onFilterOptionsDismiss() {
        savedStateHandle[SHOW_FILTER_OPTIONS] = false
    }

    fun onCycleSelect(id: Long) = viewModelScope.launch {
        val selectedIds = selectedTransactionIds.value
        transactionRepo.updateCycleForTransactions(selectedIds, id)
        dismissMultiSelectionMode()
        eventBus.send(
            AllTransactionsEvent.ShowUiMessage(
                UiText.PluralResource(
                    R.plurals.cycle_update_for_transaction,
                    selectedIds.size
                )
            )
        )
    }

    sealed interface AllTransactionsEvent {
        data class ShowUiMessage(val uiText: UiText) : AllTransactionsEvent
        data object NavigateToTagSelectionForAssignment : AllTransactionsEvent
        data class NavigateToAddEditTx(val id: Long) : AllTransactionsEvent
        data object NavigateToFolderSelection : AllTransactionsEvent
        data object ScheduleSaved : AllTransactionsEvent
        data object ChooseCycleForTransactions : AllTransactionsEvent
    }
}

private const val SEARCH_MODE_ACTIVE = "SEARCH_MODE_ACTIVE"
private const val SELECTED_CYCLE_IDS = "SELECTED_CYCLE_IDS"
private const val TRANSACTION_TYPE_FILTER = "TRANSACTION_TYPE_FILTER"
private const val SELECTED_TAG_IDS = "SELECTED_TAG_IDS"
private const val SELECTED_TRANSACTION_IDS = "SELECTED_TRANSACTION_IDS"
private const val SHOW_DELETE_TRANSACTION_CONFIRMATION = "SHOW_DELETE_TRANSACTION_CONFIRMATION"
private const val SHOW_AGGREGATION_CONFIRMATION = "SHOW_AGGREGATION_CONFIRMATION"
private const val SHOW_MULTI_SELECTION_OPTIONS = "SHOW_MULTI_SELECTION_OPTIONS"
private const val SHOW_FILTER_OPTIONS = "SHOW_FILTER_OPTIONS"