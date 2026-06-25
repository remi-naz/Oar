package dev.ridill.oar.folders.presentation.folderDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.aggregations.domain.repository.AggregationsRepository
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.domain.util.orFalse
import dev.ridill.oar.core.ui.navigation.destinations.FolderDetailsScreenSpec
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.folders.domain.model.FolderTransactionsMultiSelectionOption
import dev.ridill.oar.folders.domain.repository.FolderDetailsRepository
import dev.ridill.oar.transactions.domain.repository.AllTransactionsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: FolderDetailsRepository,
    private val aggRepo: AggregationsRepository,
    private val transactionsRepo: AllTransactionsRepository,
    private val eventBus: EventBus<FolderDetailsEvent>
) : ViewModel(), FolderDetailsActions {

    private val folderIdArg = FolderDetailsScreenSpec
        .getFolderIdArgFromSavedStateHandle(savedStateHandle)

    private val folderIdFlow = MutableStateFlow(folderIdArg)
    private val folderDetails = repo.getFolderDetailsById(folderIdArg)
    private val folderName = folderDetails
        .mapLatest { it?.name.orEmpty() }
        .distinctUntilChanged()
    private val createdTimestamp = folderDetails
        .mapLatest { it?.createdTimestamp ?: DateUtil.now() }
        .distinctUntilChanged()
    private val excluded = folderDetails
        .mapLatest { it?.excluded.orFalse() }
        .distinctUntilChanged()

    val transactionPagingData = repo.getTransactionsInFolderPaged(folderIdArg)
        .cachedIn(viewModelScope)

    private val showDeleteFolderConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_FOLDER_CONFIRMATION, false)

    private val selectedTransactionIds = savedStateHandle
        .getStateFlow<Set<Long>>(SELECTED_TRANSACTION_IDS, emptySet())
    private val transactionMultiSelectionModeActive = selectedTransactionIds
        .mapLatest { it.isNotEmpty() }
        .distinctUntilChanged()
    private val showMultiSelectionOptions = savedStateHandle
        .getStateFlow(SHOW_MULTI_SELECTION_OPTIONS, false)

    private val showDeleteTransactionsConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_TRANSACTIONS_CONFIRMATION, false)

    private val showRemoveTransactionsConfirmation = savedStateHandle
        .getStateFlow(SHOW_REMOVE_FROM_FOLDER_CONFIRMATION, false)

    private val aggregatesList = selectedTransactionIds.flatMapLatest {
        aggRepo.getAmountAggregateForTransactions(
            selectedTxIds = it,
            addExcluded = true
        )
    }.distinctUntilChanged()

    private val showAggregates = aggregatesList
        .mapLatest { it.isNotEmpty() }
        .distinctUntilChanged()

    private val shouldShowActionPreview = combineTuple(
        repo.shouldShowActionPreview(),
        transactionMultiSelectionModeActive
    ).mapLatest { (showActionPreview, multiSelectionModeActive) ->
        showActionPreview && !multiSelectionModeActive
    }.distinctUntilChanged()

    val state = combineTuple(
        folderName,
        createdTimestamp,
        excluded,
        shouldShowActionPreview,
        showDeleteFolderConfirmation,
        selectedTransactionIds,
        transactionMultiSelectionModeActive,
        showMultiSelectionOptions,
        aggregatesList,
        showAggregates,
        showDeleteTransactionsConfirmation,
        showRemoveTransactionsConfirmation
    ).mapLatest { (
                      name,
                      createdTimestamp,
                      excluded,
                      shouldShowActionPreview,
                      showDeleteConfirmation,
                      selectedTransactionIds,
                      transactionMultiSelectionModeActive,
                      showMultiSelectionOptions,
                      aggregatesList,
                      showAggregates,
                      showDeleteTransactionsConfirmation,
                      showRemoveTransactionsConfirmation
                  ) ->
        FolderDetailsState(
            folderName = name,
            createdTimestamp = createdTimestamp,
            isExcluded = excluded,
            shouldShowActionPreview = shouldShowActionPreview,
            showDeleteConfirmation = showDeleteConfirmation,
            selectedTransactionIds = selectedTransactionIds,
            transactionMultiSelectionModeActive = transactionMultiSelectionModeActive,
            showMultiSelectionOptions = showMultiSelectionOptions,
            aggregatesList = aggregatesList,
            showAggregates = showAggregates,
            showDeleteTransactionsConfirmation = showDeleteTransactionsConfirmation,
            showRemoveTransactionsConfirmation = showRemoveTransactionsConfirmation
        )
    }.asStateFlow(viewModelScope, FolderDetailsState())

    val events = eventBus.eventFlow

    override fun onCycleSelect(id: Long) {
        viewModelScope.launch {
            val cycleTransactionIds = transactionsRepo.getTransactionIdsForCycle(id)
            savedStateHandle[SELECTED_TRANSACTION_IDS] = cycleTransactionIds.toSet()
        }
    }

    override fun onDeleteClick() {
        savedStateHandle[SHOW_DELETE_FOLDER_CONFIRMATION] = true
    }

    override fun onDeleteDismiss() {
        savedStateHandle[SHOW_DELETE_FOLDER_CONFIRMATION] = false
    }

    override fun onDeleteFolderOnlyClick() {
        viewModelScope.launch {
            val id = folderIdFlow.value
            repo.deleteFolderById(id)
            savedStateHandle[SHOW_DELETE_FOLDER_CONFIRMATION] = false
            eventBus.send(FolderDetailsEvent.FolderDeleted)
        }
    }

    override fun onDeleteFolderAndTransactionsClick() {
        viewModelScope.launch {
            val id = folderIdFlow.value
            repo.deleteFolderWithTransactions(id)
            savedStateHandle[SHOW_DELETE_FOLDER_CONFIRMATION] = false
            eventBus.send(FolderDetailsEvent.FolderDeleted)
        }
    }

    private var actionPreviewDisableJob: Job? = null
    override fun onTransactionSwipeActionRevealed() {
        actionPreviewDisableJob?.cancel()
        actionPreviewDisableJob = viewModelScope.launch {
            if (shouldShowActionPreview.first()) {
                repo.disableActionPreview()
            }
        }
    }

    override fun onRemoveTransactionFromFolderClick(id: Long) {
        viewModelScope.launch {
            repo.removeTransactionFromFolderById(setOf(id))
            eventBus.send(FolderDetailsEvent.TransactionRemovedFromFolder(id))
        }
    }

    fun onRemoveTransactionUndo(txId: Long) = viewModelScope.launch {
        repo.addTransactionToFolder(txId, folderIdArg)
    }

    override fun onTransactionLongPress(id: Long) {
        savedStateHandle[SELECTED_TRANSACTION_IDS] = selectedTransactionIds.value + id
    }

    override fun onTransactionSelectionChange(id: Long) {
        val selectedIds = selectedTransactionIds.value
        savedStateHandle[SELECTED_TRANSACTION_IDS] = if (id in selectedIds) selectedIds - id
        else selectedIds + id
    }

    override fun onMultiSelectionModeDismiss() {
        savedStateHandle[SELECTED_TRANSACTION_IDS] = emptySet<Long>()
    }

    override fun onMultiSelectionOptionsClick() {
        savedStateHandle[SHOW_MULTI_SELECTION_OPTIONS] = true
    }

    override fun onMultiSelectionOptionDismiss() {
        savedStateHandle[SHOW_MULTI_SELECTION_OPTIONS] = false
    }

    override fun onMultiSelectionOptionClick(option: FolderTransactionsMultiSelectionOption) {
        when (option) {
            FolderTransactionsMultiSelectionOption.DELETE -> {
                savedStateHandle[SHOW_MULTI_SELECTION_OPTIONS] = false
                savedStateHandle[SHOW_DELETE_TRANSACTIONS_CONFIRMATION] = true
            }

            FolderTransactionsMultiSelectionOption.REMOVE_FROM_FOLDERS -> {
                savedStateHandle[SHOW_MULTI_SELECTION_OPTIONS] = false
                savedStateHandle[SHOW_REMOVE_FROM_FOLDER_CONFIRMATION] = true
            }
        }
    }

    override fun onDeleteTransactionsDismiss() {
        savedStateHandle[SHOW_DELETE_TRANSACTIONS_CONFIRMATION] = false
    }

    override fun onDeleteTransactionsConfirm() {
        viewModelScope.launch {
            val selectedIds = selectedTransactionIds.value
            repo.deleteTransactionsByIds(selectedIds)
            savedStateHandle[SELECTED_TRANSACTION_IDS] = emptySet<Long>()
            savedStateHandle[SHOW_DELETE_TRANSACTIONS_CONFIRMATION] = false
            eventBus.send(FolderDetailsEvent.ShowUiMessage(UiText.StringResource(R.string.transactions_deleted)))
        }
    }

    override fun onRemoveTransactionsFromFolderDismiss() {
        savedStateHandle[SHOW_REMOVE_FROM_FOLDER_CONFIRMATION] = false
    }

    override fun onRemoveTransactionsFromFolderConfirm() {
        viewModelScope.launch {
            val selectedIds = selectedTransactionIds.value
            repo.removeTransactionFromFolderById(selectedIds)
            savedStateHandle[SELECTED_TRANSACTION_IDS] = emptySet<Long>()
            savedStateHandle[SHOW_REMOVE_FROM_FOLDER_CONFIRMATION] = false
            eventBus.send(
                FolderDetailsEvent.ShowUiMessage(
                    UiText.PluralResource(
                        R.plurals.transaction_removed_from_this_folder,
                        selectedIds.size
                    )
                )
            )
        }
    }

    sealed interface FolderDetailsEvent {
        data class ShowUiMessage(val uiText: UiText) : FolderDetailsEvent
        data object FolderDeleted : FolderDetailsEvent
        data class TransactionRemovedFromFolder(val txId: Long) : FolderDetailsEvent
    }
}

private const val SHOW_DELETE_FOLDER_CONFIRMATION = "SHOW_DELETE_CONFIRMATION"
private const val SELECTED_TRANSACTION_IDS = "SELECTED_TRANSACTION_IDS"
private const val SHOW_MULTI_SELECTION_OPTIONS = "SHOW_MULTI_SELECTION_OPTIONS"
private const val SHOW_DELETE_TRANSACTIONS_CONFIRMATION = "SHOW_DELETE_TRANSACTIONS_CONFIRMATION"
private const val SHOW_REMOVE_FROM_FOLDER_CONFIRMATION = "SHOW_REMOVE_FROM_FOLDER_CONFIRMATION"