package dev.ridill.oar.transactions.presentation.addEditTransaction

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.service.ExpEvalService
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.domain.util.ifInfinite
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.domain.util.textAsFlow
import dev.ridill.oar.core.ui.navigation.AddEditTransactionRoute
import dev.ridill.oar.core.ui.navigation.AddEditTxResult
import dev.ridill.oar.core.ui.navigation.INVALID_ID_LONG
import dev.ridill.oar.core.ui.navigation.ScheduleInputs
import dev.ridill.oar.core.ui.navigation.TransformationResult
import dev.ridill.oar.core.ui.navigation.toScheduleInputs
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.transactions.domain.model.AmountTransformation
import dev.ridill.oar.transactions.domain.model.Transaction
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.transactions.domain.repository.AddEditTransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneOffset
import java.util.Currency

@HiltViewModel(assistedFactory = AddEditTransactionViewModel.Factory::class)
class AddEditTransactionViewModel @AssistedInject constructor(
    @Assisted val route: AddEditTransactionRoute,
    private val savedStateHandle: SavedStateHandle,
    private val cycleRepo: BudgetCycleRepository,
    private val transactionRepo: AddEditTransactionRepository,
    private val evalService: ExpEvalService,
    private val eventBus: EventBus<AddEditTransactionEvent>
) : ViewModel(), AddEditTransactionActions {

    @AssistedFactory
    interface Factory {
        fun create(route: AddEditTransactionRoute): AddEditTransactionViewModel
    }

    private val isLoading = MutableStateFlow(false)

    private val coercedIdArg: Long
        get() = route.transactionId.coerceAtLeast(OarDatabase.DEFAULT_ID_LONG)

    private val txInput = savedStateHandle.getStateFlow<Transaction?>(TX_INPUT, null)
    private val currency = txInput.mapLatest { it?.currency ?: LocaleUtil.defaultCurrency }
        .distinctUntilChanged()

    val amountInputState = savedStateHandle.saveable(
        key = "AMOUNT_INPUT_STATE",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )

    private val selectedCycleId = txInput.mapLatest { it?.cycleId }
        .distinctUntilChanged()
    private val cycleDescription = selectedCycleId
        .flatMapLatest { id ->
            id?.let(cycleRepo::getCycleByIdFlow)
                ?: flowOf(null)
        }
        .mapLatest { it?.description }
        .distinctUntilChanged()

    private val isAmountInputAnExpression = amountInputState.textAsFlow()
        .mapLatest { evalService.isExpression(it) }
        .distinctUntilChanged()

    val noteInputState = savedStateHandle.saveable(
        key = "NOTE_INPUT_STATE",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )

    private val selectedTagId = txInput.mapLatest { it?.tagId }
        .distinctUntilChanged()

    private val timestamp = txInput.mapLatest { it?.timestamp ?: DateUtil.now() }
        .distinctUntilChanged()

    private val transactionFolderId = txInput.mapLatest { it?.folderId }
        .distinctUntilChanged()

    private val fundMovement = txInput.mapLatest { it?.type ?: FundMovement.OUT }
        .distinctUntilChanged()

    private val isTransactionExcluded = txInput.mapLatest { it?.excluded == true }
        .distinctUntilChanged()

    private val showDeleteConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_CONFIRMATION, false)

    private val amountRecommendations = transactionRepo.getAmountRecommendations()

    private val showDatePicker = savedStateHandle.getStateFlow(SHOW_DATE_PICKER, false)
    private val showTimePicker = savedStateHandle.getStateFlow(SHOW_TIME_PICKER, false)

    private val linkedFolderName = transactionFolderId.flatMapLatest { selectedId ->
        transactionRepo.getFolderNameForId(selectedId)
    }.distinctUntilChanged()

    private val menuOptions = MutableStateFlow<Set<AddEditTxOption>>(emptySet())

    val state = combineTuple(
        isLoading,
        currency,
        fundMovement,
        isAmountInputAnExpression,
        amountRecommendations,
        timestamp,
        showDatePicker,
        showTimePicker,
        isTransactionExcluded,
        selectedTagId,
        showDeleteConfirmation,
        linkedFolderName,
        cycleDescription,
        selectedCycleId,
        menuOptions,
    ).mapLatest { (
                      isLoading,
                      currency,
                      transactionType,
                      isAmountInputAnExpression,
                      amountRecommendations,
                      timestamp,
                      showDatePicker,
                      showTimePicker,
                      isTransactionExcluded,
                      selectedTagId,
                      showDeleteConfirmation,
                      linkedFolderName,
                      cycleDescription,
                      selectedCycleId,
                      menuOptions,
                  ) ->
        AddEditTransactionState(
            isLoading = isLoading,
            currency = currency,
            fundMovement = transactionType,
            isAmountInputAnExpression = isAmountInputAnExpression,
            amountRecommendations = amountRecommendations,
            timestamp = timestamp,
            showDatePicker = showDatePicker,
            showTimePicker = showTimePicker,
            isTransactionExcluded = isTransactionExcluded,
            selectedTagId = selectedTagId,
            showDeleteConfirmation = showDeleteConfirmation,
            linkedFolderName = linkedFolderName,
            cycleDescription = cycleDescription,
            selectedCycleId = selectedCycleId,
            menuOptions = menuOptions,
        )
    }
        .onStart { buildMenuOptions() }
        .asStateFlow(viewModelScope, AddEditTransactionState())

    val events = eventBus.eventFlow

    init {
        onInit()
    }

    private fun onInit() {
        if (txInput.value != null) return

        viewModelScope.launch {
            val activeCycle = cycleRepo.getActiveCycle()
            var transaction = transactionRepo
                .getTransactionById(route.transactionId) ?: (txInput.value
                ?: Transaction.DEFAULT).copy(
                cycleId = activeCycle?.id ?: OarDatabase.INVALID_ID_LONG,
                currency = activeCycle?.currency ?: LocaleUtil.defaultCurrency,
            )
            if (route.isDuplicateMode) {
                transaction = transaction.copy(id = OarDatabase.DEFAULT_ID_LONG)
            }

            savedStateHandle[TX_INPUT] = transaction.copy(
                folderId = route.linkFolderId ?: transaction.folderId,
            )
            amountInputState.setTextAndPlaceCursorAtEnd(transaction.amount)
            noteInputState.setTextAndPlaceCursorAtEnd(transaction.note)
        }
    }

    private fun buildMenuOptions() {
        menuOptions.update {
            when {
                // New transaction has only schedule option
                route.transactionId == INVALID_ID_LONG -> setOf(AddEditTxOption.CREATE_SCHEDULE_FROM_TRANSACTION)
                // Duplicated transaction does not have any actions
                route.isDuplicateMode -> emptySet()
                // Existing transaction has all actions
                else -> AddEditTxOption.entries.toSet()
            }
        }
    }

    fun onCurrencySelect(currency: Currency) {
        savedStateHandle[TX_INPUT] = txInput.value?.copy(currency = currency)
    }

    fun onCycleSelect(id: Long?) {
        if (id == null) return
        savedStateHandle[TX_INPUT] = txInput.value?.copy(cycleId = id)
    }

    override fun onAmountFocusLost() {
        evaluateAmountInput()
    }

    override fun onEvaluateExpressionClick() {
        evaluateAmountInput()
    }

    private fun evaluateAmountInput() {
        val amountInput = amountInputState.text
            .trim()
            .ifEmpty { return }
            .toString()

        val isExpression = evalService.isExpression(amountInput)
        val result = if (isExpression) evalService.evalOrNull(amountInput)
        else TextFormat.parseNumber(amountInput)
        amountInputState.setTextAndPlaceCursorAtEnd(result.orZero().toString())
    }

    override fun onRecommendedAmountClick(amount: Long) {
        savedStateHandle[TX_INPUT] = txInput.value?.copy(
            amount = TextFormat.number(
                value = amount,
                isGroupingUsed = false
            )
        )
    }

    override fun onTagSelect(tagId: Long?) {
        savedStateHandle[TX_INPUT] = txInput.value?.copy(
            tagId = tagId?.takeIf { it != txInput.value?.tagId }
        )
    }

    override fun onTimestampClick() {
        savedStateHandle[SHOW_DATE_PICKER] = true
    }

    override fun onDateSelectionDismiss() {
        savedStateHandle[SHOW_DATE_PICKER] = false
    }

    override fun onDateSelectionConfirm(millis: Long) {
        savedStateHandle[TX_INPUT] = txInput.value?.copy(
            timestamp = DateUtil.dateFromMillisWithTime(
                millis = millis,
                time = txInput.value?.timestamp?.toLocalTime() ?: DateUtil.timeNow(),
                zoneId = ZoneOffset.UTC
            )
        )
        savedStateHandle[SHOW_DATE_PICKER] = false
    }

    override fun onPickTimeClick() {
        savedStateHandle[SHOW_DATE_PICKER] = false
        savedStateHandle[SHOW_TIME_PICKER] = true
    }

    override fun onTimeSelectionDismiss() {
        savedStateHandle[SHOW_TIME_PICKER] = false
    }

    override fun onTimeSelectionConfirm(hour: Int, minute: Int) {
        savedStateHandle[TX_INPUT] = txInput.value?.copy(
            timestamp = txInput.value?.timestamp
                ?.withHour(hour)
                ?.withMinute(minute)
                ?: DateUtil.now()
        )
        savedStateHandle[SHOW_TIME_PICKER] = false
    }

    override fun onPickDateClick() {
        savedStateHandle[SHOW_TIME_PICKER] = false
        savedStateHandle[SHOW_DATE_PICKER] = true
    }

    override fun onTypeChange(type: FundMovement) {
        savedStateHandle[TX_INPUT] = txInput.value?.copy(
            type = type
        )
    }

    override fun onExclusionToggle(excluded: Boolean) {
        savedStateHandle[TX_INPUT] = txInput.value?.copy(
            excluded = excluded
        )
    }

    fun onAmountTransformationResult(result: TransformationResult) {
        val amount = amountInputState.text.toString()
            .toDoubleOrNull() ?: return
        val transformedAmount = when (result.transformation) {
            AmountTransformation.DIVIDE_BY -> amount / result.factor.toDoubleOrNull()
                .orZero()

            AmountTransformation.MULTIPLIER -> amount * result.factor.toDoubleOrNull()
                .orZero()

            AmountTransformation.PERCENT -> amount * (result.factor.toFloatOrNull()
                .orZero() / 100f)
        }
        amountInputState.setTextAndPlaceCursorAtEnd(
            text = transformedAmount
                .ifInfinite { Double.Zero }
                .toString()
        )
    }

    override fun onOptionClick(option: AddEditTxOption) {
        when (option) {
            AddEditTxOption.DELETE -> {
                savedStateHandle[SHOW_DELETE_CONFIRMATION] = true
            }

            AddEditTxOption.CREATE_SCHEDULE_FROM_TRANSACTION -> {
                onConvertToScheduleOptionClick()
            }

            AddEditTxOption.DUPLICATE -> {
                onDuplicateOptionClick()
            }
        }
    }

    private fun onConvertToScheduleOptionClick() = viewModelScope.launch {
        eventBus.send(
            AddEditTransactionEvent
                .NavigateToScheduleConversion(txInput.value?.toScheduleInputs())
        )
    }

    private fun onDuplicateOptionClick() = viewModelScope.launch {
        txInput.value?.id?.let {
            eventBus.send(
                AddEditTransactionEvent
                    .NavigateToDuplicateTransactionCreation(it)
            )
        }
    }

    override fun onDeleteDismiss() {
        savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
    }

    override fun onDeleteConfirm() {
        viewModelScope.launch {
            isLoading.update { true }
            transactionRepo.deleteTransaction(coercedIdArg)
            isLoading.update { false }
            savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
            eventBus.send(AddEditTransactionEvent.NavigateUpWithResult(AddEditTxResult.TRANSACTION_DELETED))
        }
    }

    override fun onSelectFolderClick() {
        viewModelScope.launch {
            eventBus.send(AddEditTransactionEvent.LaunchFolderSelection(txInput.value?.folderId))
        }
    }

    fun onFolderSelectionResult(id: Long) {
        savedStateHandle[TX_INPUT] = txInput.value?.copy(
            folderId = id.takeIf { it != INVALID_ID_LONG }
        )
    }

    override fun onSaveClick() {
        viewModelScope.launch {
            val noteInput = noteInputState.text.trim().toString()
            val txInput = txInput.value?.copy(
                note = noteInput
            ) ?: return@launch
            val amountInput = amountInputState.text.trim().toString()
            if (amountInput.isEmpty()) {
                eventBus.send(
                    AddEditTransactionEvent.ShowUiMessage(
                        UiText.StringResource(R.string.error_invalid_amount, true)
                    )
                )
                return@launch
            }
            val isExp = evalService.isExpression(amountInput)
            val evaluatedAmount = (if (isExp) evalService.evalOrNull(amountInput)
            else TextFormat.parseNumber(amountInput)) ?: -1.0
            if (evaluatedAmount < Double.Zero) {
                eventBus.send(
                    AddEditTransactionEvent.ShowUiMessage(
                        UiText.StringResource(
                            R.string.error_invalid_amount,
                            true
                        )
                    )
                )
                return@launch
            }
            isLoading.update { true }
            transactionRepo.saveTransaction(
                transaction = txInput.copy(
                    amount = evaluatedAmount.toString(),
                )
            )
            isLoading.update { false }
            eventBus.send(AddEditTransactionEvent.NavigateUpWithResult(AddEditTxResult.TRANSACTION_SAVED))
        }
    }

    sealed interface AddEditTransactionEvent {
        data class ShowUiMessage(val uiText: UiText) : AddEditTransactionEvent
        data class NavigateUpWithResult(val result: AddEditTxResult) :
            AddEditTransactionEvent

        data class LaunchFolderSelection(val preselectedId: Long?) : AddEditTransactionEvent
        data class NavigateToDuplicateTransactionCreation(val id: Long) :
            AddEditTransactionEvent

        data class NavigateToScheduleConversion(
            val inputs: ScheduleInputs?,
        ) : AddEditTransactionEvent
    }
}

private const val TX_INPUT = "TX_INPUT"
private const val SHOW_DELETE_CONFIRMATION = "SHOW_DELETE_CONFIRMATION"
private const val SHOW_DATE_PICKER = "SHOW_DATE_PICKER"
private const val SHOW_TIME_PICKER = "SHOW_TIME_PICKER"
